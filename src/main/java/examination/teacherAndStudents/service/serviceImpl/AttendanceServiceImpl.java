package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.AttendanceAlreadyTakenException;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.EntityNotFoundException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.AttendanceService;
import examination.teacherAndStudents.utils.AttendanceStatus;
import examination.teacherAndStudents.utils.Roles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private ClassLevelRepository classLevelRepository;
    @Autowired
    private AttendancePercentRepository attendancePercentRepository;
    @Autowired
    private ClassBlockRepository classBlockRepository;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private StudentTermRepository studentTermRepository;
    @Autowired
    private AcademicSessionRepository academicSessionRepository;
    @Autowired
    private SessionClassRepository sessionClassRepository;


    public void takeBulkAttendance(BulkAttendanceRequest request) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                    .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));

          classLevelRepository.findByIdAndSchoolId(request.getClassLevelId(), admin.getSchool().getId())
                    .orElseThrow(() -> new CustomNotFoundException("Class Level  not found"));

            // Validate the student term exists
            StudentTerm studentTerm = studentTermRepository.findByIdAndAcademicSessionId(request.getStudentTermId(), request.getSessionId())
                    .orElseThrow(() -> new CustomNotFoundException("Student term not found"));

            SessionClass sessionClass = sessionClassRepository.findBySessionIdAndClassBlockId(request.getSessionId(), request.getClassBlockId())
                    .orElseThrow(() -> new CustomNotFoundException("Session class not found for Academic Session "));

            // Validate the date is within the term period
            LocalDate attendanceDate = request.getDate().toLocalDate();
            if (attendanceDate.isBefore(studentTerm.getStartDate())) {
                throw new IllegalArgumentException("Attendance date is before term start date");
            }
            if (attendanceDate.isAfter(studentTerm.getEndDate())) {
                throw new IllegalArgumentException("Attendance date is after term end date");
            }

            // Process each student attendance
            List<Attendance> attendancesToSave = new ArrayList<>();
            for (BulkAttendanceRequest.StudentAttendance sa : request.getStudentAttendances()) {
                // Verify student exists and belongs to this class
                Profile studentProfile = profileRepository.findById(sa.getStudentId())
                        .orElseThrow(() -> new CustomNotFoundException("Student not found: " + sa.getStudentId()));

                if (!studentProfile.getSessionClass().getClassBlock().getId().equals(request.getClassBlockId())) {
                    throw new IllegalArgumentException("Student doesn't belong to this class");
                }

                // Check if attendance already exists for this date/student
                boolean exists = attendanceRepository.existsByUserProfileAndDateAndAcademicYearAndStudentTerm(
                        studentProfile,
                        request.getDate(),
                        studentTerm.getAcademicSession(),
                        studentTerm);

                if (!exists) {
                    Attendance attendance = Attendance.builder()
                            .userProfile(studentProfile)
                            .sessionClass(sessionClass)
                            .academicYear(studentTerm.getAcademicSession())
                            .studentTerm(studentTerm)
                            .date(request.getDate())
                            .status(sa.getStatus())
                            .build();
                    attendancesToSave.add(attendance);
                }
            }

            // Save all new attendance records in batch
            if (!attendancesToSave.isEmpty()) {
                attendanceRepository.saveAll(attendancesToSave);
            }

        } catch (CustomNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomInternalServerException("Error taking attendance: " + e.getMessage());
        }
    }

//    calculateAttendancePercentage(student.getId(), studentClass.getId(), studentTerm.get().getId());

    @Override

    public Page<AttendanceResponses> getStudentAttendance(
            Long academicYearId,
            Long studentTermId,
            Long classBlockId,
            Long userProfileId,
            AttendanceStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        try {
            // Create Pageable object
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            SessionClass sessionClass = sessionClassRepository.findBySessionIdAndClassBlockId(academicYearId, classBlockId)
                    .orElseThrow(() -> new CustomNotFoundException("Session class not found" ));

            // Fetch filtered attendance records
            Page<Attendance> attendancePage = attendanceRepository.findAllWithFilters(
                    userProfileId,
                    sessionClass.getId(),
                    academicYearId,
                    studentTermId,
                    status,
                    startDate,
                    endDate,
                    pageable);

            // Transform to response
            return attendancePage.map(attendance -> {
                // Get all attendance records for the student for the period
                List<Attendance> allRecords = attendanceRepository.findByUserProfileAndDateBetween(
                        attendance.getUserProfile(),
                        startDate != null ? startDate : attendance.getUserProfile().getCreatedAt(),
                        endDate != null ? endDate : LocalDateTime.now());

                // Calculate statistics
                long daysPresent = allRecords.stream()
                        .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                        .count();
                long daysAbsent = allRecords.size() - daysPresent;
                double percentage = calculatePercentage(daysPresent, allRecords.size());

                // Create daily attendance map for calendar
                Map<LocalDate, AttendanceResponses.DailyAttendance> dailyAttendanceMap = allRecords.stream()
                        .collect(Collectors.toMap(
                                a -> a.getDate().toLocalDate(),
                                a -> AttendanceResponses.DailyAttendance.builder()
                                        .dateTime(a.getDate())
                                        .status(a.getStatus())
                                        .build(),
                                (existing, replacement) -> existing)); // Handle duplicate dates if needed

                return AttendanceResponses.builder()
                        .studentId(attendance.getUserProfile().getId())
                        .studentName(attendance.getUserProfile().getUser().getFirstName() + " " +
                                attendance.getUserProfile().getUser().getLastName())
                        .classSessionId(attendance.getSessionClass().getId())
                        .classBlockName(attendance.getSessionClass().getClassBlock().getName())
                        .academicYearId(attendance.getAcademicYear().getId())
                        .academicYearName(attendance.getAcademicYear().getSessionName().getName())
                        .studentTermId(attendance.getStudentTerm().getId())
                        .studentTermName(attendance.getStudentTerm().getName())
                        .daysPresent(daysPresent)
                        .daysAbsent(daysAbsent)
                        .percentageAttendance(percentage)
                        .dailyAttendance(dailyAttendanceMap)
                        .build();
            });

        } catch (Exception e) {
            throw new CustomInternalServerException("Error getting attendance records: " + e.getMessage());
        }
    }
    private double calculatePercentage(long present, long total) {
        return total > 0 ? Math.round((present * 100.0 / total) * 100.0) / 100.0 : 0;
    }

//    Get all attendance for a student: /attendance?userProfileId=123
//
//    Get present records for a class: /attendance?classBlockId=456&status=PRESENT
//
//    Get attendance for a term: /attendance?studentTermId=789&startDate=2023-09-01T00:00:00&endDate=2023-12-31T23:59:59

    public Page<AttendanceResponses> getAllStudentsAttendance(
            Long studentId,
            Long academicYearId,
            Long studentTermId,
            Long sessionId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            LocalDateTime createdAt,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        try {
            // Verify student exists if ID is provided
            if (studentId != null) {
                User student = userRepository.findById(studentId)
                        .orElseThrow(() -> new CustomNotFoundException("Student not found with ID: " + studentId));
                profileRepository.findByUser(student)
                        .orElseThrow(() -> new CustomNotFoundException("Student profile not found with ID: " + studentId));
            }

            // Create Pageable object
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            // Fetch filtered and paginated results
            Page<Attendance> attendances = attendanceRepository.findAllWithFilters(
                    studentId,
                    academicYearId,
                    studentTermId,
                    sessionId,
                    startDate,
                    endDate,
                    createdAt,
                    pageable);

            // Map to response DTO
            return attendances.map(this::toAttendanceResponse);
        } catch (CustomNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomInternalServerException("Error getting attendance records: " + e.getMessage());
        }
    }

    public List<Attendance> getStudentAttendanceByClass(Long classId, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            List<Profile> studentsInClass = profileRepository.findBySessionClassId(classId);
            return attendanceRepository.findByUserProfileInAndDateBetween(studentsInClass, startDate, endDate);
        } catch (Exception e) {
            // Handle exceptions
            throw new CustomInternalServerException("Error getting student attendance by class: " + e.getMessage());
        }
    }

    public StudentAttendanceResponse calculateAttendancePercentage(Long userId, Long classLevelId, Long sessionId, Long studentTermId) {
        try {
            Optional<User> optionalStudent = userRepository.findById(userId);
            if (optionalStudent.isEmpty()) {
                throw new CustomNotFoundException("Student not found with ID: " + userId);
            }

            User student = optionalStudent.get();
            Optional<Profile> studentProfile = profileRepository.findByUser(student);
            if (studentProfile.isEmpty()) {
                throw new CustomNotFoundException("Profile not found for student with ID: " + userId);
            }

            SessionClass sessionClass = sessionClassRepository.findBySessionIdAndClassBlockId(sessionId, classLevelId)
                    .orElseThrow(() -> new CustomNotFoundException("Class Level not found with ID: " + classLevelId));

            Optional<examination.teacherAndStudents.entity.StudentTerm> studentTerm = studentTermRepository.findById(studentTermId);
            if (studentTerm.isEmpty()) {
                throw new CustomNotFoundException("Student term not found with ID: " + studentTermId);
            }

            // Get term start and end dates
            LocalDate startDate = studentTerm.get().getStartDate();
            LocalDate endDate = studentTerm.get().getEndDate();
            if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
                throw new CustomInternalServerException("Invalid term start or end date");
            }

            LocalDateTime startDateTime = startDate.atStartOfDay(); // Start of the day
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX); // End of the day (23:59:59.999)

            // Check if the attendance percentage already exists
            Optional<AttendancePercent> existingAttendancePercent = attendancePercentRepository.findByUserAndSessionClassIdAndStudentTermId(studentProfile.get(),sessionClass.getId(), studentTerm.get().getId());

            // Get the total number of attendance records for the user within the term's date range
            long totalAttendanceRecords = attendanceRepository.countByUserProfileIdAndStudentTermAndDateRange(
                    studentProfile.get().getId(), studentTerm.get(), startDateTime, endDateTime);

            // Get the number of days the student attended within the term's date range
            long daysPresent = attendanceRepository.countByUserProfileIdAndStudentTermAndStatusAndDateRange(
                    studentProfile.get().getId(), studentTerm.get(), AttendanceStatus.PRESENT, startDateTime, endDateTime);

            // Calculate days absent
            long daysAbsent = totalAttendanceRecords - daysPresent;

            // Check if totalAttendanceRecords is zero to avoid division by zero
            if (totalAttendanceRecords == 0) {
                throw new CustomInternalServerException("No attendance records found for the term. Cannot calculate percentage.");
            }

            // Calculate the attendance percentage
            double attendancePercentage = (double) daysPresent / totalAttendanceRecords * 100;

            // Round the attendance percentage to the nearest whole number
            double roundedPercentage = (double) Math.round(attendancePercentage);

            // Save or update the attendance percentage in the AttendancePercent entity
            AttendancePercent attendancePercent = existingAttendancePercent.orElse(new AttendancePercent());
            attendancePercent.setAttendancePercentage(roundedPercentage);
            attendancePercent.setStudentTerm(studentTerm.get());
            attendancePercent.setUser(studentProfile.get());
            attendancePercent.setAcademicYear(sessionClass.getAcademicSession());
            attendancePercent.setSessionClass(sessionClass);

            attendancePercentRepository.save(attendancePercent);

            // The Profile entity you already have
            ProfileData profileData = new ProfileData(
                    studentProfile.get().getId(),
                    studentProfile.get().getUniqueRegistrationNumber(),
                    studentProfile.get().getPhoneNumber()
            );

            // Return StudentAttendanceResponse with student profile, calculated percentage, and attendance details
            return new StudentAttendanceResponse(profileData, roundedPercentage, daysPresent, daysAbsent, totalAttendanceRecords);

        } catch (CustomNotFoundException e) {
            throw new CustomNotFoundException("An error occurred: " + e.getMessage());
        } catch (Exception e) {
            throw new CustomInternalServerException("An error occurred while calculating attendance percentage: " + e.getMessage());
        }
    }

    public List<StudentAttendanceResponse> calculateClassAttendancePercentage(Long classLevelId, Long sessionId, Long termId) {
        List<StudentAttendanceResponse> attendanceResponses = new ArrayList<>();

        try {
            SessionClass sessionClass = sessionClassRepository.findBySessionIdAndClassBlockId(sessionId,classLevelId)
                    .orElseThrow(() -> new CustomNotFoundException("Class Level not found"));

            examination.teacherAndStudents.entity.StudentTerm studentTerm = studentTermRepository.findById(termId)
                    .orElseThrow(() -> new CustomNotFoundException("Student Term not found with ID: " + termId));

            // Validate term start and end dates
            LocalDate startDate = studentTerm.getStartDate();
            LocalDate endDate = studentTerm.getEndDate();
            if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
                throw new CustomInternalServerException("Invalid term start or end date");
            }
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

            Set<Profile> students = sessionClass.getProfiles();

            for (Profile studentProfile : students) {
                // Get total attendance records within the term's date range
                long totalAttendanceRecords = attendanceRepository.countByUserProfileIdAndStudentTermAndDateRange(
                        studentProfile.getId(), studentTerm, startDateTime, endDateTime);

                // Get days attended within the term's date range
                long daysPresent = attendanceRepository.countByUserProfileIdAndStudentTermAndStatusAndDateRange(
                        studentProfile.getId(), studentTerm, AttendanceStatus.PRESENT, startDateTime, endDateTime);

                // Calculate days absent
                long daysAbsent = totalAttendanceRecords - daysPresent;

                double attendancePercentage = 0.0;
                if (totalAttendanceRecords != 0) {
                    attendancePercentage = (double) daysPresent / totalAttendanceRecords * 100;
                }

                double roundedPercentage = Math.round(attendancePercentage);

                AttendancePercent attendancePercent = attendancePercentRepository.findByUserAndSessionClassIdAndStudentTermId(studentProfile,sessionClass.getId(), studentTerm.getId())
                        .orElse(new AttendancePercent());

                attendancePercent.setAttendancePercentage(roundedPercentage);
                attendancePercent.setStudentTerm(studentTerm);
                attendancePercent.setUser(studentProfile);
                attendancePercent.setAcademicYear(sessionClass.getAcademicSession());
                attendancePercent.setSessionClass(sessionClass);

                attendancePercentRepository.save(attendancePercent);

                ProfileData profileData = new ProfileData(
                        studentProfile.getId(),
                        studentProfile.getUniqueRegistrationNumber(),
                        studentProfile.getPhoneNumber()
                );

                // Add the StudentAttendanceResponse to the list with new fields
                attendanceResponses.add(new StudentAttendanceResponse(
                        profileData,
                        roundedPercentage,
                        daysPresent,
                        daysAbsent,
                        totalAttendanceRecords
                ));
            }
        } catch (CustomNotFoundException e) {
            throw new CustomNotFoundException("An error occurred: " + e.getMessage());
        } catch (Exception e) {
            throw new CustomInternalServerException("An error occurred while calculating attendance percentages: " + e.getMessage());
        }

        return attendanceResponses;
    }

    private AttendanceResponses toAttendanceResponse(Attendance attendance) {
        return AttendanceResponses.builder()
                .id(attendance.getId())
                .studentId(attendance.getUserProfile().getId())
                .studentName(attendance.getUserProfile().getUser().getFirstName() + " " + attendance.getUserProfile().getUser().getLastName())
                .classSessionId(attendance.getSessionClass().getId())
                .classBlockName(attendance.getSessionClass().getClassBlock().getName())
                .academicYearId(attendance.getAcademicYear().getId())
                .academicYearName(attendance.getAcademicYear().getSessionName().getName())
                .studentTermId(attendance.getStudentTerm().getId())
                .studentTermName(attendance.getStudentTerm().getName())
                .date(attendance.getDate())
                .status(attendance.getStatus())
                .createdAt(attendance.getCreatedAt())
                .updatedAt(attendance.getUpdatedAt())
                .build();
    }


    // Add additional methods as needed, such as getting attendance for a specific date or class.
}


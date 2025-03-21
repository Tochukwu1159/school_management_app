package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.ProfileData;
import examination.teacherAndStudents.dto.SubjectScheduleTeacherUpdateDto;
import examination.teacherAndStudents.dto.TeacherAttendanceRequest;
import examination.teacherAndStudents.dto.TeacherAttendanceResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.*;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.TeacherAttendanceService;
import examination.teacherAndStudents.utils.AttendanceStatus;
import examination.teacherAndStudents.utils.Roles;
import examination.teacherAndStudents.utils.TeachingStatus;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TeacherAttendanceServiceImpl implements TeacherAttendanceService {
    @Autowired
    private TeacherAttendanceRepository teacherAttendanceRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SubjectScheduleRepository subjectScheduleRepository;
    @Autowired
    private TimetableRepository timetableRepository;
    @Autowired
    private TeacherAttendancePercentRepository teacherAttendancePercentRepository;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private StudentTermRepository studentTermRepository;
    @Autowired
    private AcademicSessionRepository academicSessionRepository;

    @Override
    public void takeTeacherAttendance(TeacherAttendanceRequest attendanceRequest) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);

            if (admin == null) {
                throw new CustomNotFoundException("Please login as an Admin"); // Return unauthorized response for non-admin users
            }

            User teacher = userRepository.findByIdAndRoles(attendanceRequest.getTeacherId(), Roles.TEACHER);

            Optional<Profile> teacherProfile = profileRepository.findByUser(teacher);

            if (teacher == null) {
                throw new EntityNotFoundException("Teacher not found with ID: " + attendanceRequest.getTeacherId());
            }
            Optional<examination.teacherAndStudents.entity.StudentTerm> studentTerm = studentTermRepository.findById(attendanceRequest.getStudentTermId());
            Optional<examination.teacherAndStudents.entity.AcademicSession> session = academicSessionRepository.findById(attendanceRequest.getSessionId());


            if (studentTerm.isEmpty()) {
                throw new EntityNotFoundException("StudentTerm not found with ID: " + attendanceRequest.getStudentTermId());
            }

            // Check if attendanceDate is within the startDate and endDate of StudentTerm
            LocalDate attendanceDate = attendanceRequest.getAttendanceDate().toLocalDate();
            LocalDate startDate = studentTerm.get().getStartDate();
            LocalDate endDate = studentTerm.get().getEndDate();

            if (attendanceDate.isBefore(startDate) || attendanceDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Attendance date " + attendanceDate + " is outside the Student term period (" + startDate + " to " + endDate + ")");
            }


            TeacherAttendance existingAttendance = teacherAttendanceRepository.findByTeacherAndDateAndAcademicYearAndStudentTerm(teacherProfile.get(), attendanceRequest.getAttendanceDate(),session.get(),studentTerm.get());


            if (existingAttendance != null) {
                // Attendance for the given date already exists, throw a custom exception
                throw new AttendanceAlreadyTakenException("Attendance for date " + attendanceRequest.getAttendanceDate() + " already taken for student ID: " + teacher.getId());
            } else {

                TeacherAttendance attendanceRecord = new TeacherAttendance();
                attendanceRecord.setTeacher(teacherProfile.get());
                attendanceRecord.setStudentTerm(studentTerm.get());
                attendanceRecord.setAcademicYear(studentTerm.get().getAcademicSession());
                attendanceRecord.setDate(attendanceRequest.getAttendanceDate());
                attendanceRecord.setStatus(attendanceRequest.getStatus());

                teacherAttendanceRepository.save(attendanceRecord);
            }

        } catch (CustomNotFoundException e) {
            throw new CustomNotFoundException("Error occurred " + e.getMessage());
        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException("Error occurred " + e.getMessage());
        } catch (Exception e) {
            throw new NotFoundException("An error occurred while taking teacher attendance." + e.getMessage());
        }
    }

    public TeacherAttendanceResponse calculateAttendancePercentage(Long userId, Long sessionId, Long term) {
        try {
            Optional<User> optionalTeacher = userRepository.findById(userId);

            if (optionalTeacher.isEmpty()) {
                throw new NotFoundException("Teacher not found");
            }

            Optional<AcademicSession> session = academicSessionRepository.findById(sessionId);
            Optional<examination.teacherAndStudents.entity.StudentTerm> studentTerm = studentTermRepository.findById(term);

            Optional<Profile> teacherProfile = profileRepository.findByUser(optionalTeacher.get());

            if (teacherProfile.isEmpty()) {
                throw new NotFoundException("Teacher not found");
            }

            // Check if the attendance percentage already exists
            Optional<TeacherAttendancePercent> existingAttendancePercent = teacherAttendancePercentRepository.findByTeacherAndStudentTerm(teacherProfile.get(), studentTerm.get());

            // Get the total number of attendance records for the teacher
            long totalAttendanceRecords = teacherAttendanceRepository.countByTeacherIdAndStudentTerm(teacherProfile.get().getId(), studentTerm.get());

            // Get the number of days the teacher attended
            long daysAttended = teacherAttendanceRepository.countByTeacherIdAndStudentTermAndAndStatus(teacherProfile.get().getId(), studentTerm.get(), AttendanceStatus.PRESENT);

            // Check if totalAttendanceRecords is zero to avoid division by zero
            if (totalAttendanceRecords == 0) {
                throw new CustomInternalServerException("Total attendance records are zero. Cannot calculate percentage.");
            }

            // Calculate the attendance percentage
            double attendancePercentage = (double) daysAttended / totalAttendanceRecords * 100;

            // Round the attendance percentage to the nearest whole number
            double roundedPercentage = (double) Math.round(attendancePercentage);

            // Save or update the attendance percentage in the TeacherAttendancePercent entity
            TeacherAttendancePercent attendancePercent = existingAttendancePercent.orElse(new TeacherAttendancePercent());

            attendancePercent.setTeacher(teacherProfile.get());
            attendancePercent.setStudentTerm(studentTerm.get());
            attendancePercent.setAttendancePercentage(roundedPercentage);
            attendancePercent.setAcademicYear(session.get());

            teacherAttendancePercentRepository.save(attendancePercent);
            ProfileData profileData = new ProfileData(
                    teacherProfile.get().getId(),
                    teacherProfile.get().getUniqueRegistrationNumber(),
                    teacherProfile.get().getPhoneNumber()
            );

            // Return TeacherAttendanceResponse with teacher profile and calculated percentage
            return new TeacherAttendanceResponse(profileData, roundedPercentage);

        } catch (CustomNotFoundException e) {
            throw new CustomNotFoundException("An error occurred: " + e.getMessage());
        } catch (Exception e) {
            throw new CustomInternalServerException("An error occurred while calculating attendance percentage: " + e.getMessage());
        }
    }

    public List<TeacherAttendanceResponse> calculateTeacherAttendancePercentage(Long sessionId, Long termId) {
        try {
            Optional<AcademicSession> session = academicSessionRepository.findById(sessionId);
            Optional<examination.teacherAndStudents.entity.StudentTerm> studentTerm = studentTermRepository.findById(termId);

            if (session.isEmpty()) {
                throw new NotFoundException("Academic session not found");
            }

            if (studentTerm.isEmpty()) {
                throw new NotFoundException("Student term not found");
            }

            // Get all teachers
            List<User> teachers = userRepository.findAllByRoles(Roles.TEACHER);

            List<TeacherAttendanceResponse> teacherAttendanceResponses = new ArrayList<>();

            for (User teacher : teachers) {
                Optional<Profile> teacherProfile = profileRepository.findById(teacher.getId());

                if (teacherProfile.isPresent()) {
                    // Check if attendance percentage already exists for this teacher and term
                    Optional<TeacherAttendancePercent> existingAttendancePercent = teacherAttendancePercentRepository
                            .findByTeacherAndStudentTerm(teacherProfile.get(), studentTerm.get());

                    // Get the total number of attendance records for the teacher
                    long totalAttendanceRecords = teacherAttendanceRepository
                            .countByTeacherIdAndStudentTerm(teacherProfile.get().getId(), studentTerm.get());

                    // Get the number of days the teacher attended
                    long daysAttended = teacherAttendanceRepository
                            .countByTeacherIdAndStudentTermAndAndStatus(teacherProfile.get().getId(), studentTerm.get(), AttendanceStatus.PRESENT);

                    // Check if totalAttendanceRecords is zero to avoid division by zero
//                    if (totalAttendanceRecords == 0) {
//                        continue; // Skip this teacher as their attendance data is not available
//                    }

                    // Calculate the attendance percentage
                    double attendancePercentage = (double) daysAttended / totalAttendanceRecords * 100;

                    // Round the attendance percentage to the nearest whole number
                    double roundedPercentage = Math.round(attendancePercentage);

                    // Save or update the attendance percentage in the TeacherAttendancePercent entity
                    TeacherAttendancePercent attendancePercent = existingAttendancePercent.orElse(new TeacherAttendancePercent());

                    attendancePercent.setTeacher(teacherProfile.get());
                    attendancePercent.setStudentTerm(studentTerm.get());
                    attendancePercent.setAttendancePercentage(roundedPercentage);
                    attendancePercent.setAcademicYear(session.get());

                    teacherAttendancePercentRepository.save(attendancePercent);

                    ProfileData profileData = new ProfileData(
                            teacherProfile.get().getId(),
                            teacherProfile.get().getUniqueRegistrationNumber(),
                            teacherProfile.get().getPhoneNumber()
                    );

                    // Add the response to the list
                    teacherAttendanceResponses.add(new TeacherAttendanceResponse(profileData, roundedPercentage));
                }
            }

            // Return the list of TeacherAttendanceResponse objects
            return teacherAttendanceResponses;

        } catch (Exception e) {
            throw new CustomInternalServerException("An error occurred while calculating teacher attendance percentage: " + e.getMessage());
        }
    }



    @Override
    public List<TeacherAttendance> getAllTeacherAttendance() {
        try {
            return teacherAttendanceRepository.findAll();
        } catch (Exception e) {
            throw new CustomInternalServerException("An error occurred while fetching all teacher attendance: " + e.getMessage());
        }
    }

    @Override
    public List<TeacherAttendance> getTeacherAttendanceByDateRange(LocalDate startDate, LocalDate endDate) {
        try {
            if (startDate == null || endDate == null) {
                throw new IllegalArgumentException("Start date and end date cannot be null");
            }
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Start date must be earlier than end date");
            }
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

            return teacherAttendanceRepository.findByDateBetween(startDateTime, endDateTime);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Error occurred: " + e.getMessage());
        } catch (Exception e) {
            throw new CustomInternalServerException("An error occurred while fetching teacher attendance: " + e.getMessage());
        }
    }


    public List<TeacherAttendance> getTeacherAttendanceByTeacherAndDateRange(
            Long teacherId,
            LocalDate startDate,
            LocalDate endDate) {
        try {
            if (teacherId == null || startDate == null || endDate == null) {
                throw new IllegalArgumentException("Teacher ID, start date, and end date cannot be null");
            }
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Start date must be earlier than end date");
            }
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            // Fetch teacher by ID
            User teacher = userRepository.findByIdAndRoles(teacherId, Roles.TEACHER);
            if (teacher == null) {
                throw new EntityNotFoundException("Teacher not found with ID: " + teacherId);
            }
            Optional<Profile> teacherProfile = profileRepository.findByUser(teacher);

            // Fetch teacher attendance records
            return teacherAttendanceRepository.findByTeacherIdAndDateBetween(teacherProfile.get().getId(), startDateTime, endDateTime);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Error occurred: " + e.getMessage());
        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException("Error occurred: " + e.getMessage());
        } catch (Exception e) {
            throw new CustomInternalServerException("An error occurred while fetching teacher attendance: " + e.getMessage());
        }



    }
}

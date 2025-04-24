package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.ProfileData;
import examination.teacherAndStudents.dto.TeacherAttendanceRequest;
import examination.teacherAndStudents.dto.TeacherAttendanceResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.*;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.TeacherAttendanceService;
import examination.teacherAndStudents.utils.AttendanceStatus;
import examination.teacherAndStudents.utils.Roles;
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
    private TeacherAttendancePercentRepository teacherAttendancePercentRepository;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private StudentTermRepository studentTermRepository;
    @Autowired
    private AcademicSessionRepository academicSessionRepository;

    @Override
    public void takeTeacherAttendance(TeacherAttendanceRequest attendanceRequest) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));

        Optional<User> teacher = userRepository.findByIdAndRole(attendanceRequest.getTeacherId(), Roles.TEACHER);
        if (teacher == null) {
            throw new EntityNotFoundException("Teacher not found with ID: " + attendanceRequest.getTeacherId());
        }

        Profile teacherProfile = profileRepository.findByUser(teacher.get())
                .orElseThrow(() -> new EntityNotFoundException("Profile not found for teacher ID: " + teacher.get().getId()));

        examination.teacherAndStudents.entity.StudentTerm studentTerm = studentTermRepository.findById(attendanceRequest.getStudentTermId())
                .orElseThrow(() -> new EntityNotFoundException("StudentTerm not found with ID: " + attendanceRequest.getStudentTermId()));

        examination.teacherAndStudents.entity.AcademicSession session = academicSessionRepository.findById(attendanceRequest.getSessionId())
                .orElseThrow(() -> new EntityNotFoundException("Academic session not found with ID: " + attendanceRequest.getSessionId()));

        // Validate attendance date
        LocalDate attendanceDate = attendanceRequest.getAttendanceDate().toLocalDate();
        if (attendanceDate.isBefore(studentTerm.getStartDate()) || attendanceDate.isAfter(studentTerm.getEndDate())) {
            throw new IllegalArgumentException("Attendance date " + attendanceDate + " is outside the Student term period ("
                    + studentTerm.getStartDate() + " to " + studentTerm.getEndDate() + ")");
        }

        // Check if attendance already exists
        if (teacherAttendanceRepository.existsByTeacherAndDateAndAcademicYearAndStudentTerm(
                teacherProfile, attendanceRequest.getAttendanceDate(), session, studentTerm)) {
            throw new AttendanceAlreadyTakenException("Attendance for date " + attendanceRequest.getAttendanceDate()
                    + " already taken for teacher ID: " + teacher.get().getId());
        }

        // Save attendance
        TeacherAttendance attendanceRecord = new TeacherAttendance();
        attendanceRecord.setTeacher(teacherProfile);
        attendanceRecord.setStudentTerm(studentTerm);
        attendanceRecord.setAcademicYear(session);
        attendanceRecord.setDate(attendanceRequest.getAttendanceDate());
        attendanceRecord.setStatus(attendanceRequest.getStatus());

        teacherAttendanceRepository.save(attendanceRecord);
    }



    public TeacherAttendanceResponse calculateAttendancePercentage(Long userId, Long sessionId, Long termId) {
        User teacher = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Teacher not found"));

        AcademicSession session = academicSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Academic session not found"));

        examination.teacherAndStudents.entity.StudentTerm studentTerm = studentTermRepository.findById(termId)
                .orElseThrow(() -> new NotFoundException("Student term not found"));

        Profile teacherProfile = profileRepository.findByUser(teacher)
                .orElseThrow(() -> new NotFoundException("Teacher profile not found"));

        // Fetch all attendance records at once
        List<TeacherAttendance> attendanceRecords = teacherAttendanceRepository
                .findByTeacherIdAndAcademicYearAndStudentTerm(teacherProfile.getId(),session, studentTerm);

        if (attendanceRecords.isEmpty()) {
            throw new CustomInternalServerException("No attendance records found. Cannot calculate percentage.");
        }

        // Calculate present days
        long daysAttended = attendanceRecords.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                .count();

        long daysAbsent = attendanceRecords.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                .count();

        long daysLate = attendanceRecords.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.LATE)
                .count();

        // Calculate percentage
        double attendancePercentage = (int) Math.round((double) daysAttended / attendanceRecords.size() * 100);

        // Save or update attendance percentage
        TeacherAttendancePercent attendancePercent = teacherAttendancePercentRepository
                .findByTeacherAndStudentTerm(teacherProfile, studentTerm)
                .orElse(new TeacherAttendancePercent());

        attendancePercent.setTeacher(teacherProfile);
        attendancePercent.setStudentTerm(studentTerm);
        attendancePercent.setDaysPresent(daysAttended);
        attendancePercent.setDaysLate(daysLate);
        attendancePercent.setDaysAbsent(daysAbsent);
        attendancePercent.setAttendancePercentage(attendancePercentage);
        attendancePercent.setAcademicYear(session);
        teacherAttendancePercentRepository.save(attendancePercent);

        ProfileData profileData = new ProfileData(
                teacherProfile.getId(),
                teacherProfile.getUniqueRegistrationNumber(),
                teacherProfile.getPhoneNumber()
        );

        return new TeacherAttendanceResponse(profileData, attendancePercentage);
    }


    public List<TeacherAttendanceResponse> calculateTeacherAttendancePercentage(Long sessionId, Long termId) {
        AcademicSession session = academicSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Academic session not found"));

        examination.teacherAndStudents.entity.StudentTerm studentTerm = studentTermRepository.findById(termId)
                .orElseThrow(() -> new NotFoundException("Student term not found"));

        // Get all teachers
        List<User> teachers = userRepository.findUsersByRole(Roles.TEACHER);
        List<TeacherAttendanceResponse> teacherAttendanceResponses = new ArrayList<>();

        for (User teacher : teachers) {
            Profile teacherProfile = profileRepository.findById(teacher.getId())
                    .orElseThrow(() -> new NotFoundException("Teacher profile not found for user ID: " + teacher.getId()));

            // Fetch all attendance records at once
            List<TeacherAttendance> attendanceRecords = teacherAttendanceRepository
                    .findByTeacherIdAndAcademicYearAndStudentTerm(teacherProfile.getId(), session, studentTerm);

            long totalAttendanceRecords = attendanceRecords.size();
            long daysAttended = attendanceRecords.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                    .count();
            long daysAbsent = attendanceRecords.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                    .count();

            long daysLate = attendanceRecords.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.LATE)
                    .count();

            // Calculate attendance percentage
            double attendancePercentage = (totalAttendanceRecords == 0) ? 0
                    : (int) Math.round((double) daysAttended / totalAttendanceRecords * 100);

            // Save or update attendance percentage
            TeacherAttendancePercent attendancePercent = teacherAttendancePercentRepository
                    .findByTeacherAndStudentTerm(teacherProfile, studentTerm)
                    .orElse(new TeacherAttendancePercent());

            attendancePercent.setTeacher(teacherProfile);
            attendancePercent.setStudentTerm(studentTerm);
            attendancePercent.setAttendancePercentage(attendancePercentage);
            attendancePercent.setDaysPresent(daysAttended);
            attendancePercent.setDaysAbsent(daysAbsent);
            attendancePercent.setDaysLate(daysLate);
            attendancePercent.setAcademicYear(session);
            teacherAttendancePercentRepository.save(attendancePercent);

            ProfileData profileData = new ProfileData(
                    teacherProfile.getId(),
                    teacherProfile.getUniqueRegistrationNumber(),
                    teacherProfile.getPhoneNumber()
            );

            teacherAttendanceResponses.add(new TeacherAttendanceResponse(profileData, attendancePercentage));
        }

        return teacherAttendanceResponses;
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
            Optional<User> teacher = userRepository.findByIdAndRole(teacherId, Roles.TEACHER);
            if (teacher == null) {
                throw new EntityNotFoundException("Teacher not found with ID: " + teacherId);
            }
            Optional<Profile> teacherProfile = profileRepository.findByUser(teacher.get());

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

package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.ProfileData;
import examination.teacherAndStudents.dto.StaffAttendanceRequest;
import examination.teacherAndStudents.dto.StaffAttendanceResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.*;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.StaffAttendanceService;
import examination.teacherAndStudents.utils.AttendanceStatus;
import examination.teacherAndStudents.utils.Roles;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffAttendanceServiceImpl implements StaffAttendanceService {
    private final StaffAttendanceRepository staffAttendanceRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final StaffAttendancePercentRepository staffAttendancePercentRepository;
    private final StudentTermRepository studentTermRepository;
    private final AcademicSessionRepository academicSessionRepository;

    @Override
    @Transactional
    public void takeStaffAttendance(StaffAttendanceRequest attendanceRequest) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));

        User staff = userRepository.findByIdAndSchoolId(attendanceRequest.getStaffId(), admin.getSchool().getId())
                .orElseThrow(() -> new CustomNotFoundException("Staff not found with ID: " + attendanceRequest.getStaffId()));

        Profile staffProfile = profileRepository.findByUser(staff)
                .orElseThrow(() -> new CustomNotFoundException("Profile not found for staff ID: " + staff.getId()));

        StudentTerm studentTerm = studentTermRepository.findByIdAndAcademicSessionId(attendanceRequest.getStudentTermId(), attendanceRequest.getSessionId())
                .orElseThrow(() -> new CustomNotFoundException("StudentTerm not found with ID: " + attendanceRequest.getStudentTermId()));

        // Validate attendance date
        LocalDate attendanceDate = attendanceRequest.getAttendanceDate().toLocalDate();
        if (attendanceDate.isBefore(studentTerm.getStartDate()) || attendanceDate.isAfter(studentTerm.getEndDate())) {
            throw new IllegalArgumentException("Attendance date " + attendanceDate + " is outside the term period ("
                    + studentTerm.getStartDate() + " to " + studentTerm.getEndDate() + ")");
        }

        // Check if attendance already exists
        if (staffAttendanceRepository.existsByStaffAndDateAndAcademicYearAndStudentTerm(
                staffProfile, attendanceRequest.getAttendanceDate(), studentTerm.getAcademicSession(), studentTerm)) {
            throw new AttendanceAlreadyTakenException("Attendance for date " + attendanceRequest.getAttendanceDate()
                    + " already taken for staff ID: " + staff.getId());
        }

        // Save attendance
        StaffAttendance attendanceRecord = StaffAttendance.builder()
                .staff(staffProfile)
                .studentTerm(studentTerm)
                .academicYear(studentTerm.getAcademicSession())
                .date(attendanceRequest.getAttendanceDate())
                .status(attendanceRequest.getStatus())
                .build();

        staffAttendanceRepository.save(attendanceRecord);
    }

    @Transactional()
    public StaffAttendanceResponse calculateAttendancePercentage(Long userId, Long sessionId, Long termId) {
        // Fetch authenticated user's school (assuming admin role)
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));

        // Fetch teacher
        User teacher = userRepository.findById(userId)
                .orElseThrow(() -> new CustomNotFoundException("Teacher not found with ID: " + userId));

        // Validate school consistency
        if (!admin.getSchool().getId().equals(teacher.getSchool().getId())) {
            throw new BadRequestException("Teacher belongs to a different school.");
        }

        // Fetch academic session
        AcademicSession session = academicSessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomNotFoundException("Academic session not found with ID: " + sessionId));

        // Validate session school
        if (!admin.getSchool().getId().equals(session.getSchool().getId())) {
            throw new BadRequestException("Academic session belongs to a different school.");
        }

        // Fetch student term
        StudentTerm studentTerm = studentTermRepository.findById(termId)
                .orElseThrow(() -> new CustomNotFoundException("Student term not found with ID: " + termId));

        // Validate term school and session
        if (!admin.getSchool().getId().equals(studentTerm.getAcademicSession().getSchool().getId())) {
            throw new BadRequestException("Student term belongs to a different school.");
        }
        if (!studentTerm.getAcademicSession().getId().equals(session.getId())) {
            throw new IllegalArgumentException("Student term does not belong to the provided academic session.");
        }

        // Fetch teacher profile
        Profile teacherProfile = profileRepository.findByUser(teacher)
                .orElseThrow(() -> new CustomNotFoundException("Teacher profile not found for user ID: " + userId));

        // Fetch attendance records
        List<StaffAttendance> attendanceRecords = staffAttendanceRepository
                .findByStaffAndAcademicYearAndStudentTerm(teacherProfile, session, studentTerm);

        if (attendanceRecords.isEmpty()) {
            throw new CustomNotFoundException(
                    String.format("No attendance records found for teacher ID %d, session ID %d, term ID %d",
                            userId, sessionId, termId));
        }

        // Calculate attendance counts in one pass
        Map<AttendanceStatus, Long> attendanceCounts = attendanceRecords.stream()
                .collect(Collectors.groupingBy(
                        StaffAttendance::getStatus,
                        Collectors.counting()
                ));

        long daysAttended = attendanceCounts.getOrDefault(AttendanceStatus.PRESENT, 0L);
        long daysAbsent = attendanceCounts.getOrDefault(AttendanceStatus.ABSENT, 0L);
        long daysLate = attendanceCounts.getOrDefault(AttendanceStatus.LATE, 0L);
        long totalDays = daysAttended + daysAbsent + daysLate;

        // Calculate percentage
        double attendancePercentage = Math.round((double) daysAttended / attendanceRecords.size() * 100.0 * 100.0) / 100.0;

        // Save or update attendance percentage
        StaffAttendancePercent attendancePercent = staffAttendancePercentRepository
                .findByStaffAndStudentTerm(teacherProfile, studentTerm)
                .orElse(new StaffAttendancePercent());

        attendancePercent.setStaff(teacherProfile);
        attendancePercent.setStudentTerm(studentTerm);
        attendancePercent.setDaysPresent(daysAttended);
        attendancePercent.setDaysLate(daysLate);
        attendancePercent.setDaysAbsent(daysAbsent);
        attendancePercent.setTotalDays(totalDays);
        attendancePercent.setAttendancePercentage(attendancePercentage);
        attendancePercent.setAcademicYear(session);
        staffAttendancePercentRepository.save(attendancePercent);

        // Build response
        ProfileData profileData = new ProfileData(
                teacherProfile.getId(),
                teacherProfile.getUniqueRegistrationNumber(),
                teacherProfile.getPhoneNumber()
        );

        return new StaffAttendanceResponse(profileData, attendancePercentage, daysAttended, daysAbsent, daysLate, totalDays);
    }
    @Override
    @Transactional
    public List<StaffAttendanceResponse> calculateStaffAttendancePercentage(Long sessionId, Long termId, String role) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                .orElseThrow(() -> new CustomNotFoundException("Admin not found"));

        StudentTerm studentTerm = studentTermRepository.findByIdAndAcademicSessionId(termId, sessionId)
                .orElseThrow(() -> new CustomNotFoundException("Student term not found with ID: " + termId));

        List<User> staffList;
        Long schoolId = admin.getSchool().getId();
        if (role != null && !role.isEmpty()) {
            try {
                Roles roleEnum = Roles.valueOf(role.toUpperCase());
                if (roleEnum == Roles.STUDENT || roleEnum == Roles.ADMIN) {
                    throw new IllegalArgumentException("Invalid role: " + role + ". Cannot query STUDENT or ADMIN roles.");
                }
                staffList = userRepository.findBySchoolIdAndRoles(schoolId, roleEnum);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role: " + role);
            }
        } else {
            List<Roles> excludedRoles = Arrays.asList(Roles.STUDENT, Roles.ADMIN);
            staffList = userRepository.findBySchoolIdAndRolesNotIn(schoolId, excludedRoles);
        }

        List<StaffAttendanceResponse> attendanceResponses = new ArrayList<>();
        for (User staff : staffList) {
            Profile staffProfile = profileRepository.findByUser(staff)
                    .orElseThrow(() -> new CustomNotFoundException("Staff profile not found for ID: " + staff.getId()));

            // Fetch attendance records
            List<StaffAttendance> attendanceRecords = staffAttendanceRepository
                    .findByStaffAndAcademicYearAndStudentTerm(staffProfile, studentTerm.getAcademicSession(), studentTerm);

            double attendancePercentage;
            long daysAttended = 0, daysAbsent = 0, daysLate = 0, totalDays = 0;
            if (!attendanceRecords.isEmpty()) {
                daysAttended = attendanceRecords.stream()
                        .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                        .count();
                daysAbsent = attendanceRecords.stream()
                        .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                        .count();
                daysLate = attendanceRecords.stream()
                        .filter(a -> a.getStatus() == AttendanceStatus.LATE)
                        .count();
                attendancePercentage = Math.round((double) daysAttended / attendanceRecords.size() * 100.0 * 100.0) / 100.0;
                totalDays = daysAttended + daysAbsent + daysLate;
            } else {
                attendancePercentage = 0.0;
            }

            // Save or update attendance percentage
            StaffAttendancePercent attendancePercent = staffAttendancePercentRepository
                    .findByStaffAndStudentTerm(staffProfile, studentTerm)
                    .orElse(StaffAttendancePercent.builder()
                            .staff(staffProfile)
                            .studentTerm(studentTerm)
                            .academicYear(studentTerm.getAcademicSession())
                            .build());

            attendancePercent.setAttendancePercentage(attendancePercentage);
            attendancePercent.setDaysPresent(daysAttended);
            attendancePercent.setDaysAbsent(daysAbsent);
            attendancePercent.setDaysLate(daysLate);
            staffAttendancePercentRepository.save(attendancePercent);

            ProfileData profileData = new ProfileData(
                    staffProfile.getId(),
                    staffProfile.getUniqueRegistrationNumber(),
                    staffProfile.getPhoneNumber()
            );

            attendanceResponses.add(new StaffAttendanceResponse(
                    profileData,
                    attendancePercentage,
                    daysAttended,
                    daysAbsent,
                    daysLate,
                    totalDays
            ));
        }

        return attendanceResponses;
    }
    @Override
    public List<StaffAttendance> getAllStaffAttendance() {
        return List.of();
    }

    @Override
    public List<StaffAttendance> getStaffAttendanceByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be earlier than end date");
        }
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        return staffAttendanceRepository.findByDateBetween(startDateTime, endDateTime);
    }

    @Override
    public List<StaffAttendance> getStaffAttendanceByStaffAndDateRange(Long staffId, LocalDate startDate, LocalDate endDate) {
        if (staffId == null || startDate == null || endDate == null) {
            throw new IllegalArgumentException("Staff ID, start date, and end date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be earlier than end date");
        }

        User staff = userRepository.findByIdAndRole(staffId, Roles.TEACHER)
                .orElseThrow(() -> new CustomNotFoundException("Staff not found with ID: " + staffId));
        Profile staffProfile = profileRepository.findByUser(staff)
                .orElseThrow(() -> new CustomNotFoundException("Staff profile not found for ID: " + staffId));

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        return staffAttendanceRepository.findByStaffAndDateBetween(staffProfile, startDateTime, endDateTime);
    }

    @Override
    public Page<StaffAttendance> getAllStaffAttendance(int pageNo, int pageSize, String sortBy) {
        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());
        return staffAttendanceRepository.findAll(paging);
    }

    @Override
    public Page<StaffAttendance> getStaffAttendanceByDateRange(LocalDate startDate, LocalDate endDate, int pageNo, int pageSize, String sortBy) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be earlier than end date");
        }
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());
        return staffAttendanceRepository.findByDateBetween(startDateTime, endDateTime, paging);
    }
}
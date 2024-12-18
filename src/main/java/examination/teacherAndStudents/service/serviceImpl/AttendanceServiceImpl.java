package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.AttendanceRequest;
import examination.teacherAndStudents.dto.AttendanceResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.AttendanceAlreadyTakenException;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.AttendanceService;
import examination.teacherAndStudents.utils.AttendanceStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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


    public void takeAttendance(AttendanceRequest attendanceRequest) {
        try {
            // Retrieve the student from the database
            User student = userRepository.findById(attendanceRequest.getStudentId())
                    .orElseThrow(() -> new CustomNotFoundException("Student not found with ID: " + attendanceRequest.getStudentId()));

            Profile studentProfle = profileRepository.findByUser(student)
                    .orElseThrow(() -> new CustomNotFoundException("Student not found with ID: " + attendanceRequest.getStudentId()));

            ClassBlock studentClass = classBlockRepository.findById(studentProfle.getClassBlock().getId())
                    .orElseThrow(() -> new CustomNotFoundException("Class not found with ID: " + studentProfle.getClassBlock().getClassLevel().getId()));

            ClassLevel generalClass  = classLevelRepository.findById(studentClass.getClassLevel().getId())
                    .orElseThrow(() -> new CustomNotFoundException("Class is not found in supposed class level"));


            // Check if attendance for the given date and student already exists
            Attendance existingAttendance = attendanceRepository.findByUserProfileAndDate(studentProfle, attendanceRequest.getDate());
            Optional<examination.teacherAndStudents.entity.StudentTerm> studentTerm = studentTermRepository.findById(attendanceRequest.getStudentTermId());


            if (existingAttendance != null) {
                // Attendance for the given date already exists, throw a custom exception
                throw new AttendanceAlreadyTakenException("Attendance for date " + attendanceRequest.getDate() + " already taken for student ID: " + student.getId());
        } else {
                // Create a new attendance entry
                Attendance attendance = new Attendance();
                attendance.setUserProfile(studentProfle);
                attendance.setStudentTerm(studentTerm.get());
                attendance.setClassBlock(studentClass);
                attendance.setDate(attendanceRequest.getDate());
                attendance.setStatus(attendanceRequest.getStatus());
                attendanceRepository.save(attendance);
            }

            // After recording attendance, update the attendance percentage
            calculateAttendancePercentage(student.getId(), studentClass.getId(), studentTerm.get().getId());

        } catch (CustomNotFoundException e) {
            // Handle not found exception
            throw new CustomNotFoundException("Error: " + e.getMessage());
        } catch (Exception e) {
            // Handle other exceptions
            throw new CustomInternalServerException("Error taking attendance: " + e.getMessage());
        }
    }




    @Override

    public AttendanceResponse getStudentAttendance(Long studentId, LocalDate startDate, LocalDate endDate) {
        try {
            // Retrieve the student from the database
            User student = userRepository.findById(studentId)
                    .orElseThrow(() -> new CustomNotFoundException("Student not found with ID: " + studentId));

            Profile studentProfile = profileRepository.findById(studentId)
                    .orElseThrow(() -> new CustomNotFoundException("Student profile not found with ID: " + studentId));

            // Retrieve all attendance records for the student within the specified date range
            List<Attendance> attendanceRecords = attendanceRepository.findByUserProfileAndDateBetween(studentProfile, startDate, endDate);

            // Calculate number of days present and absent
            long daysPresent = attendanceRecords.stream()
                    .filter(attendance -> attendance.getStatus() == AttendanceStatus.PRESENT)
                    .count();
            long daysAbsent = attendanceRecords.size() - daysPresent;

            // Calculate percentage attendance
            double totalAttendanceDays = ChronoUnit.DAYS.between(startDate, endDate) + 1; // Adding 1 to include endDate
            double percentageAttendance = (daysPresent / totalAttendanceDays) * 100;

            Double roundedPercentage = (double) Math.round(percentageAttendance);

            // Group attendance records by day of the week
            Map<DayOfWeek, List<Attendance>> attendanceByDayOfWeek = attendanceRecords.stream()
                    .collect(Collectors.groupingBy(attendance -> attendance.getDate().getDayOfWeek()));

            // Construct the AttendanceResponse
            AttendanceResponse response = new AttendanceResponse();
            response.setStudentName(student.getFirstName() + student.getLastName());
            response.setStudentId(studentId);
            response.setDaysPresent(daysPresent);
            response.setDaysAbsent(daysAbsent);
            response.setPercentageAttendance(roundedPercentage);
            response.setAttendanceByDayOfWeek(attendanceByDayOfWeek);

            return response;
        } catch (CustomNotFoundException e) {
            // Handle custom not found exception
            throw e;
        } catch (Exception e) {
            // Handle other exceptions
            throw new CustomInternalServerException("Error getting student attendance: " + e.getMessage());
        }
    }

    public List<Attendance> getAllStudentsAttendance(Long studentId, LocalDate startDate, LocalDate endDate) {
        try {
            // Retrieve the student from the database
            User student = userRepository.findById(studentId)
                    .orElseThrow(() -> new CustomNotFoundException("Student not found with ID: " + studentId));

            Profile studentProfile = profileRepository.findByUser(student)
                    .orElseThrow(() -> new CustomNotFoundException("Student profile not found with ID: " + studentId));

            // Retrieve attendance records for the student within the specified date range
            return attendanceRepository.findByUserProfileAndDateBetween(studentProfile, startDate, endDate);
        } catch (CustomNotFoundException e) {
            // Handle custom not found exception
            throw e;
        } catch (Exception e) {
            // Handle other exceptions
            throw new CustomInternalServerException("Error getting student attendance: " + e.getMessage());
        }
    }

    public List<Attendance> getStudentAttendanceByClass(Long classId, LocalDate startDate, LocalDate endDate) {
        try {
            List<Profile> studentsInClass = profileRepository.findByClassBlockId(classId);
            return attendanceRepository.findByUserProfileInAndDateBetween(studentsInClass, startDate, endDate);
        } catch (Exception e) {
            // Handle exceptions
            throw new CustomInternalServerException("Error getting student attendance by class: " + e.getMessage());
        }
    }

    public double calculateAttendancePercentage(Long userId, Long classLevelId, Long studentTermId) {
        try {
            Optional<User> optionalStudent = userRepository.findById(userId);

          Optional<Profile> studentProfile = profileRepository.findByUser(optionalStudent.get());


            if (optionalStudent.isEmpty()) {
                throw new CustomNotFoundException("Student not found");
            }
            ClassBlock classLevel = classBlockRepository.findById(classLevelId)
                    .orElseThrow(() -> new CustomNotFoundException("Class Level not found with ID: " + classLevelId));

            ClassLevel generalClass = classLevelRepository.findById(classLevelId)
                    .orElseThrow(() -> new CustomNotFoundException("Class with ID: " + classLevelId + " not found in class level"));

            User student = optionalStudent.get();
            Optional<examination.teacherAndStudents.entity.StudentTerm> studentTerm = studentTermRepository.findById(studentTermId);

            // Check if the attendance percentage already exists
            Optional<AttendancePercent> existingAttendancePercent = attendancePercentRepository.findByUserAndStudentTerm(student, studentTerm.get());

            // Get the total number of attendance records for the user
            long totalAttendanceRecords = attendanceRepository.countByUserProfileIdAndStudentTerm(studentProfile.get().getId(), studentTerm.get());

            // Get the number of days the user attended
            long daysAttended = attendanceRepository.countByUserProfileIdAndStudentTermAndStatus(studentProfile.get().getId(), studentTerm.get(), AttendanceStatus.PRESENT);

            // Check if totalAttendanceRecords is zero to avoid division by zero
            if (totalAttendanceRecords == 0) {
                throw new CustomInternalServerException("Total attendance records are zero. Cannot calculate percentage.");
            }

            // Calculate the attendance percentage
            double attendancePercentage = (double) daysAttended / totalAttendanceRecords * 100;

            // Round the attendance percentage to the nearest whole number
            Double roundedPercentage = (double) Math.round(attendancePercentage);

            // Save the attendance percentage in the AttendancePercent entity
            AttendancePercent attendancePercent = existingAttendancePercent.orElse(new AttendancePercent());

            attendancePercent.setAttendancePercentage(roundedPercentage);
            attendancePercent.setStudentTerm(studentTerm.get());
            attendancePercent.setUser(student);
            attendancePercent.setClassBlock(classLevel);

            attendancePercentRepository.save(attendancePercent);

            return roundedPercentage;
        } catch (CustomNotFoundException e) {
            throw new CustomNotFoundException("An error occurred: " + e.getMessage());
        } catch (Exception e) {
            throw new CustomInternalServerException("An error occurred while calculating attendance percentage: " + e.getMessage());
        }
    }




    // Add additional methods as needed, such as getting attendance for a specific date or class.
}


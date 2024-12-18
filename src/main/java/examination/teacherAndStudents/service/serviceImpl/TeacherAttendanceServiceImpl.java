package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.SubjectScheduleTeacherUpdateDto;
import examination.teacherAndStudents.dto.TeacherAttendanceRequest;
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
    private AttendancePercentRepository attendancePercentRepository;
    @Autowired
    private TeacherAttendancePercentRepository teacherAttendancePercentRepository;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private StudentTermRepository studentTermRepository;

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
            TeacherAttendance existingAttendance = teacherAttendanceRepository.findByTeacherAndDate(teacher, attendanceRequest.getAttendanceDate());
            Optional<examination.teacherAndStudents.entity.StudentTerm> studentTerm = studentTermRepository.findById(attendanceRequest.getStusentTermId());


            if (existingAttendance != null) {
                // Attendance for the given date already exists, throw a custom exception
                throw new AttendanceAlreadyTakenException("Attendance for date " + attendanceRequest.getAttendanceDate() + " already taken for student ID: " + teacher.getId());
            } else {

                TeacherAttendance attendanceRecord = new TeacherAttendance();
                attendanceRecord.setTeacher(teacherProfile.get());
                attendanceRecord.setStudentTerm(studentTerm.get());
                attendanceRecord.setDate(attendanceRequest.getAttendanceDate());
                attendanceRecord.setStatus(attendanceRequest.getStatus());

                teacherAttendanceRepository.save(attendanceRecord);
            }
            // After recording attendance, update the attendance percentage
            calculateAttendancePercentage(teacher.getId(),studentTerm.get().getId());

        } catch (CustomNotFoundException e) {
            throw new CustomNotFoundException("Error occurred " +e.getMessage());
        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException("Error occurred " +e.getMessage());
        } catch (Exception e) {
            throw new NotFoundException("An error occurred while taking teacher attendance." + e.getMessage());
        }
    }

    public double calculateAttendancePercentage(Long userId, Long term) {
        try {
            Optional<User> optionalTeacher = userRepository.findById(userId);

            if (optionalTeacher.isEmpty()) {
                throw new CustomNotFoundException("Teacher not found");
            }

            Optional<examination.teacherAndStudents.entity.StudentTerm> studentTerm = studentTermRepository.findById(term);

            Optional<Profile> teacherProfile = profileRepository.findById(userId);

            if (teacherProfile.isEmpty()) {
                throw new CustomNotFoundException("Teacher not found");
            }

            // Check if the attendance percentage already exists
            Optional<TeacherAttendancePercent> existingAttendancePercent = teacherAttendancePercentRepository.findByTeacherAndStudentTerm(teacherProfile.get(), studentTerm.get());

            // Get the total number of attendance records for the user
            long totalAttendanceRecords = teacherAttendanceRepository.countByTeacherIdAndStudentTerm(teacherProfile.get().getId(), studentTerm.get());

            // Get the number of days the user attended
            long daysAttended = teacherAttendanceRepository.countByTeacherIdAndStudentTermAndAndStatus(teacherProfile.get().getId(), studentTerm.get(), AttendanceStatus.PRESENT);

            // Check if totalAttendanceRecords is zero to avoid division by zero
            if (totalAttendanceRecords == 0) {
                throw new CustomInternalServerException("Total attendance records are zero. Cannot calculate percentage.");
            }

            // Calculate the attendance percentage
            double attendancePercentage = (double) daysAttended / totalAttendanceRecords * 100;

            // Round the attendance percentage to the nearest whole number
            Double roundedPercentage = (double) Math.round(attendancePercentage);

            // Save the attendance percentage in the TeacherAttendancePercent entity
            TeacherAttendancePercent attendancePercent = existingAttendancePercent.orElse(new TeacherAttendancePercent());

            attendancePercent.setTeacher(teacherProfile.get());
            attendancePercent.setStudentTerm(studentTerm.get());
            attendancePercent.setAttendancePercentage(roundedPercentage);

            teacherAttendancePercentRepository.save(attendancePercent);

            return roundedPercentage;
        } catch (CustomNotFoundException e) {
            throw new CustomNotFoundException("An error occurred: " + e.getMessage());
        } catch (Exception e) {
            throw new CustomInternalServerException("An error occurred while calculating attendance percentage: " + e.getMessage());
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
    


    @Transactional
    public SubjectSchedule updateTeachingStatus(SubjectScheduleTeacherUpdateDto updateDto) {
        try {
            // Retrieve the subject schedule by ID
            SubjectSchedule subjectSchedule = subjectScheduleRepository.findByIdAndTimetableDayOfWeek(updateDto.getScheduleId(), updateDto.getDayOfWeek());
            if(subjectSchedule == null){
                throw  new CustomNotFoundException("Ensure the time table day match the schedule for the day");
            }
            Timetable timetable = timetableRepository.findBySubjectSchedules(subjectSchedule);

           //  Check if the class has already been taught
            if (subjectSchedule.getTeachingStatus() == TeachingStatus.COMPLETED) {
                throw new CustomInternalServerException("This class has already been taught.");
            }
            // Update the topic and teaching status
            subjectSchedule.setTopic(updateDto.getTopic());
            subjectSchedule.setSubject(subjectSchedule.getSubject());
            subjectSchedule.setStartTime(subjectSchedule.getStartTime());
            subjectSchedule.setEndTime(subjectSchedule.getEndTime());
            subjectSchedule.setTimetable(timetable);
            subjectSchedule.setTeachingStatus(TeachingStatus.COMPLETED);
            subjectSchedule.setTeachersUpdatedTime(LocalDateTime.now());

            // Save the updated subject schedule
            return subjectScheduleRepository.save(subjectSchedule);
        } catch (Exception e) {
            // Handle or log the exception as needed
            throw new CustomInternalServerException("An error occurred while updating teacher taught topic: " + e.getMessage());
        }
    }


    @Transactional
    public List<SubjectSchedule> getAllNotTaughtSubjectSchedules() {
        return subjectScheduleRepository.findAllByTeachingStatus(TeachingStatus.MISSED);
    }

    @Transactional
    public List<SubjectSchedule> getAllTaughtSubjectSchedules() {
        return subjectScheduleRepository.findAllByTeachingStatus(TeachingStatus.COMPLETED);
    }



}

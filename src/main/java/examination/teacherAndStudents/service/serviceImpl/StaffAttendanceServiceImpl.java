package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.SubjectScheduleTeacherUpdateDto;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.EntityNotFoundException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.StaffAttendanceService;
import examination.teacherAndStudents.utils.AttendanceStatus;
import examination.teacherAndStudents.utils.Roles;
import examination.teacherAndStudents.utils.TeachingStatus;
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
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StaffAttendanceServiceImpl implements StaffAttendanceService {
    private final StaffAttendanceRepository staffAttendanceRepository;

    private final UserRepository userRepository;
    private final SubjectScheduleRepository subjectScheduleRepository;
    private final TimetableRepository timetableRepository;
    private final AttendancePercentRepository attendancePercentRepository;
    private final StaffAttendancePercentRepository staffAttendancePercentRepository;
    private final ProfileRepository profileRepository;


    public void checkIn(String location) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User staff = userRepository.findByEmailAndRoles(email, Roles.TEACHER);

            if (staff == null) {
                throw new CustomNotFoundException("Please log in as a teacher."); // Improved error message
            }
            Optional<Profile> staffProfile = profileRepository.findByUser(staff);

            if (staffProfile == null) {
                throw new CustomNotFoundException("Staff not found"); // Improved error message
            }

            LocalDateTime checkInTime = LocalDateTime.now();

            // Check if the teacher has already checked in for the day
            StaffAttendance existingAttendance = staffAttendanceRepository
                    .findFirstByStaffAndCheckInTimeBetweenOrderByCheckInTimeDesc(
                            staff,
                            LocalDate.now().atStartOfDay(),
                            LocalDate.now().atTime(23, 59, 59)
                    )
                    .orElse(null);

            if (existingAttendance != null && existingAttendance.getCheckOutTime() == null) {
                throw new RuntimeException("Teacher has already checked in for the day.");
            }

            StaffAttendance attendance = new StaffAttendance();
            attendance.setStaffUniqueRegNumber(staffProfile.get().getUniqueRegistrationNumber());
            attendance.setCheckInTime(checkInTime);
            attendance.setCheckInLocation(location);
            attendance.setStaff(staffProfile.get());
            attendance.setStatus(AttendanceStatus.PRESENT);
//            GeoLocation geoLocation =
            staffAttendanceRepository.save(attendance);
        } catch (Exception e) {
            // Handle exceptions appropriately (log, rethrow, etc.)
            throw new RuntimeException("Error during check-in process.", e);
        }
    }

    public void checkOut(String location) {
        List<String> errors = new ArrayList<>();

        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User staff = userRepository.findByEmailAndRoles(email, Roles.TEACHER);

            if (staff == null) {
                errors.add("Please log in as a teacher.");
            }

            LocalDateTime checkOutTime = LocalDateTime.now();

            // Find the latest attendance record for the teacher
            StaffAttendance attendance = staffAttendanceRepository
                    .findFirstByStaffOrderByCheckInTimeDesc(staff)
                    .orElseThrow(() -> new RuntimeException("Staff has not checked in."));

            // Check if the teacher has already checked out
            if (attendance.getCheckOutTime() != null) {
                errors.add("Staff has already checked out.");
            }

            // Check if the teacher has checked in before allowing check-out
            if (attendance.getCheckInTime() == null) {
                errors.add("Staff has not checked in.");
            }

            if (!errors.isEmpty()) {
                throw new RuntimeException(String.join(" ", errors));
            }

            attendance.setCheckOutTime(checkOutTime);
            attendance.setCheckOutLocation(location);

            staffAttendanceRepository.save(attendance);
        } catch (Exception e) {
            errors.add("Error during check-out process.");
            // Log the exception or handle it based on your requirements
        } finally {
            if (!errors.isEmpty()) {
                throw new RuntimeException(String.join(" ", errors));
            }
        }
    }




    @Override
    public Page<StaffAttendance> getAllStaffAttendance(int pageNo, int pageSize, String sortBy) {
        try {
            Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());

            return staffAttendanceRepository.findAll(paging);
        } catch (Exception e) {
            throw new CustomInternalServerException("An error occurred while fetching all teacher attendance: " + e.getMessage());
        }
    }


    @Override
    public Page<StaffAttendance> getStaffAttendanceByDateRange(LocalDate startDate, LocalDate endDate, int pageNo, int pageSize, String sortBy) {
        try {
            if (startDate == null || endDate == null) {
                throw new IllegalArgumentException("Start date and end date cannot be null");
            }
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Start date must be earlier than end date");
            }
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());

            return staffAttendanceRepository.findAllByCheckInTimeBetween( startDateTime, endDateTime, paging);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Error occurred: " + e.getMessage());
        } catch (Exception e) {
            throw new CustomInternalServerException("An error occurred while fetching teacher attendance: " + e.getMessage());
        }
    }




    public List<StaffAttendance> getStaffAttendanceByStaffAndDateRange(
            String staffId,
            LocalDate startDate,
            LocalDate endDate) {
        try {
            if (staffId == null || startDate == null || endDate == null) {
                throw new IllegalArgumentException("Teacher ID, start date, and end date cannot be null");
            }
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Start date must be earlier than end date");
            }
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            // Fetch teacher by ID
            Optional<Profile> staff = profileRepository.findByUniqueRegistrationNumber(staffId);
            if (staff == null) {
                throw new EntityNotFoundException("Teacher not found with ID: " + staffId);
            }
            // Fetch teacher attendance records
            return staffAttendanceRepository.findAllByStaffUniqueRegistrationNumberAndAndCheckInTimeBetween(staffId, startDateTime, endDateTime);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Error occurred: " + e.getMessage());
        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException("Error occurred: " + e.getMessage());
        } catch (Exception e) {
            throw new CustomInternalServerException("An error occurred while fetching teacher attendance: " + e.getMessage());
        }



    }
    


    @Transactional
    public SubjectSchedule updateStaffStatus(SubjectScheduleTeacherUpdateDto updateDto) {
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
        return subjectScheduleRepository.findAllByTeachingStatus(TeachingStatus.COMPLETED);
    }

    @Transactional
    public List<SubjectSchedule> getAllTaughtSubjectSchedules() {
        return subjectScheduleRepository.findAllByTeachingStatus(TeachingStatus.MISSED);
    }



}

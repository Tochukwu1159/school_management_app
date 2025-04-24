package examination.teacherAndStudents.service.serviceImpl;

import com.google.api.client.util.Value;
import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.BiometricVerificationResult;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.EntityNotFoundException;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.BiometricService;
import examination.teacherAndStudents.service.StaffAttendanceService;
import examination.teacherAndStudents.utils.AttendanceStatus;
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
    private final ProfileRepository profileRepository;
    private final BiometricService biometricService;
    private final BiometricTemplateRepository biometricTemplateRepository;

    @Value("${biometric_verification_threshold:0.7}")
    private double verificationThreshold;

    @Transactional
    public void checkIn(byte[] thumbprintData, String deviceId) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        Profile staffProfile = profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("Staff profile not found"));

        // Verify thumbprint
        BiometricVerificationResult result = biometricService.verifyThumbprint(
                thumbprintData,
                staffProfile.getId()
        );

        if (!result.isVerified() || result.getScore() < verificationThreshold) {
            throw new CustomInternalServerException(
                    "Thumbprint verification failed. Score: " + result.getScore()
            );
        }

        // Check for existing attendance
        if (hasActiveCheckIn(staffProfile)) {
            throw new NotFoundException("Staff has active check-in without check-out");
        }

        StaffAttendance attendance = new StaffAttendance();
        attendance.setStaffUniqueRegNumber(staffProfile.getUniqueRegistrationNumber());
        attendance.setCheckInTime(LocalDateTime.now());
        attendance.setStaff(staffProfile);
        attendance.setStatus(AttendanceStatus.PRESENT);
        attendance.setThumbprintHash(result.getTemplateHash());
        attendance.setBiometricDeviceId(deviceId);
        attendance.setVerificationScore(result.getScore());

        staffAttendanceRepository.save(attendance);
    }

    @Transactional
    public void checkOut(byte[] thumbprintData, String deviceId) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        Profile staffProfile = profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("Staff profile not found"));

        // Verify thumbprint
        BiometricVerificationResult result = biometricService.verifyThumbprint(
                thumbprintData,
                Long.valueOf(staffProfile.getUniqueRegistrationNumber())
        );

        if (!result.isVerified() || result.getScore() < verificationThreshold) {
            throw new NotFoundException(
                    "Thumbprint verification failed. Score: " + result.getScore()
            );
        }

        Optional<StaffAttendance> attendance1 = staffAttendanceRepository
                .findFirstByStaffAndCheckOutTimeIsNullOrderByCheckInTimeDesc(staffProfile);

       StaffAttendance attendance = attendance1.get();

        attendance.setCheckOutTime(LocalDateTime.now());
        attendance.setCheckOutThumbprintHash(result.getTemplateHash());
        attendance.setCheckOutBiometricDeviceId(deviceId);
        attendance.setCheckOutVerificationScore(result.getScore());

        staffAttendanceRepository.save(attendance);
    }

    public boolean hasActiveCheckIn(Profile staff) {
        if (staff == null) {
            throw new IllegalArgumentException("Staff cannot be null");
        }

        // Check for any attendance record without check-out time
        return staffAttendanceRepository.existsByStaffAndCheckOutTimeIsNull(staff);
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
            if (staff.isEmpty()) {
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



}

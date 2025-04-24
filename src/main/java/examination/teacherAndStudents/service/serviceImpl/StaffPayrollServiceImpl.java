package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.StaffLevelResponse;
import examination.teacherAndStudents.dto.StaffPayrollRequest;
import examination.teacherAndStudents.dto.StaffPayrollResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.StaffLevelService;
import examination.teacherAndStudents.service.StaffPayrollService;
import examination.teacherAndStudents.utils.Roles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StaffPayrollServiceImpl implements StaffPayrollService {

    private static final String STAFF_NOT_FOUND = "Staff not found with ID: ";
    private static final String PROFILE_NOT_FOUND = "Staff profile not found";
    private static final String STAFF_LEVEL_NOT_FOUND = "Staff level not found";
    private static final String PAYROLL_NOT_FOUND = "Payroll not found";
    private static final String ADMIN_NOT_AUTHORIZED = "Admin not found or not authorized";
    private static final String INVALID_MONTH = "Month must be between 1 and 12";

    private final StaffPayrollRepository staffPayrollRepository;
    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final ProfileRepository profileRepository;
    private final StaffLevelService staffLevelService;
    private final StaffLevelRepository staffLevelRepository;

    @Override
    @Transactional
    public StaffPayrollResponse createOrUpdatePayroll(StaffPayrollRequest payrollRequest) {
        log.info("Creating/updating payroll for staff ID: {}", payrollRequest.getStaffId());

        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                .orElseThrow(() -> new CustomNotFoundException(ADMIN_NOT_AUTHORIZED));

        Profile userProfile = getStaffProfile(payrollRequest.getStaffId());
        StaffLevelResponse staffLevel = validateStaffLevel(userProfile);
        School school = admin.getSchool();

        StaffPayroll payroll = staffPayrollRepository.findByStaffIdAndSchoolId(userProfile.getId(), school.getId())
                .orElseGet(() -> createNewPayroll(userProfile, school));

        updatePayrollDetails(payroll, payrollRequest, staffLevel, userProfile, school);

        StaffPayroll savedPayroll = staffPayrollRepository.save(payroll);
        log.info("Successfully saved payroll with ID: {}", savedPayroll.getId());

        return mapToStaffPayrollResponse(savedPayroll);
    }

    @Override
    @Transactional(readOnly = true)
    public StaffPayrollResponse getPayrollForStaff(Long staffId) {
        log.info("Fetching payroll for staff ID: {}", staffId);
        StaffPayroll payroll = staffPayrollRepository.findByStaffId(staffId)
                .orElseThrow(() -> new CustomNotFoundException(PAYROLL_NOT_FOUND));
        return mapToStaffPayrollResponse(payroll);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffPayrollResponse> getAllPayroll() {
        log.info("Fetching all payroll records");
        return staffPayrollRepository.findAll().stream()
                .map(this::mapToStaffPayrollResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffPayrollResponse> getPayrollForSchool() {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        log.info("Fetching payroll for school of admin: {}", email);

        User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                .orElseThrow(() -> new CustomNotFoundException(ADMIN_NOT_AUTHORIZED));

        return staffPayrollRepository.findBySchoolId(admin.getSchool().getId()).stream()
                .map(this::mapToStaffPayrollResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffPayrollResponse> getPayrollForMonth(int month, int year) {
        log.info("Fetching payroll for month: {}, year: {}", month, year);

        validateMonth(month);

        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                .orElseThrow(() -> new CustomNotFoundException(ADMIN_NOT_AUTHORIZED));

        return staffPayrollRepository.findBySchoolIdAndYearAndMonth(admin.getSchool().getId(), month, year).stream()
                .map(this::mapToStaffPayrollResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deletePayroll(Long payrollId) {
        log.info("Deleting payroll with ID: {}", payrollId);
        if (!staffPayrollRepository.existsById(payrollId)) {
            throw new CustomNotFoundException(PAYROLL_NOT_FOUND);
        }
        staffPayrollRepository.deleteById(payrollId);
    }

    @Override
    @Transactional
    public String promoteStaff(Long userId, Long staffLevelId) {
        log.info("Promoting staff with ID: {} to level ID: {}", userId, staffLevelId);

        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomNotFoundException(PROFILE_NOT_FOUND + userId));

        StaffLevel newStaffLevel = staffLevelRepository.findById(staffLevelId)
                .orElseThrow(() -> new CustomNotFoundException(STAFF_LEVEL_NOT_FOUND + staffLevelId));

        profile.setStaffLevel(newStaffLevel);
        profileRepository.save(profile);

        log.info("Successfully promoted staff with ID: {}", userId);
        return "Staff promoted successfully";
    }

    // Helper methods
    private Profile getStaffProfile(Long staffId) {
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new CustomNotFoundException(STAFF_NOT_FOUND + staffId));

        return profileRepository.findByUser(staff)
                .orElseThrow(() -> new CustomNotFoundException(PROFILE_NOT_FOUND));
    }

    private StaffLevelResponse validateStaffLevel(Profile userProfile) {
        StaffLevelResponse staffLevel = staffLevelService.getStaffLevelById(userProfile.getStaffLevel().getId());
        if (staffLevel == null) {
            throw new CustomNotFoundException(STAFF_LEVEL_NOT_FOUND);
        }
        return staffLevel;
    }

    private StaffPayroll createNewPayroll(Profile userProfile, School school) {
        return StaffPayroll.builder()
                .staff(userProfile)
                .school(school)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private void updatePayrollDetails(StaffPayroll payroll, StaffPayrollRequest request,
                                      StaffLevelResponse staffLevel, Profile userProfile, School school) {
        payroll.setName(userProfile.getUser().getFirstName() + " " + userProfile.getUser().getLastName());
        payroll.setUniqueRegistrationNumber(userProfile.getUniqueRegistrationNumber());
        payroll.setBonuses(request.getBonuses());
        payroll.setDeductions(request.getDeductions());
        payroll.setBaseSalary(staffLevel.getBaseSalary());
        payroll.setGrossPay(staffLevel.getGrossSalary());
        payroll.setNetPay(staffLevel.getNetSalary());
        payroll.setTax(staffLevel.getTax());
        payroll.setHmo(staffLevel.getHmo());
        payroll.setRemarks(request.getRemarks());
        payroll.setUpdatedAt(LocalDateTime.now());
    }

    private StaffPayrollResponse mapToStaffPayrollResponse(StaffPayroll payroll) {
        return StaffPayrollResponse.builder()
                .id(payroll.getId())
                .name(payroll.getName())
                .uniqueRegistrationNumber(payroll.getUniqueRegistrationNumber())
                .baseSalary(payroll.getBaseSalary())
                .bonuses(payroll.getBonuses())
                .deductions(payroll.getDeductions())
                .hmo(payroll.getHmo())
                .grossPay(payroll.getGrossPay())
                .tax(payroll.getTax())
                .netPay(payroll.getNetPay())
                .datePayed(payroll.getDatePayed())
                .remarks(payroll.getRemarks())
                .staffId(payroll.getStaff().getId())
                .schoolId(payroll.getSchool().getId())
                .createdAt(payroll.getCreatedAt())
                .updatedAt(payroll.getUpdatedAt())
                .build();
    }

    private void validateMonth(int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException(INVALID_MONTH);
        }
    }
}
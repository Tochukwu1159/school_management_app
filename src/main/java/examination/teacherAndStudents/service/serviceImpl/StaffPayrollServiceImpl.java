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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StaffPayrollServiceImpl implements StaffPayrollService {

    @Autowired
    private StaffPayrollRepository staffPayrollRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SchoolRepository schoolRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private StaffLevelService staffLevelService;
    @Autowired
    private StaffLevelRepository staffLevelRepository;

    // Create or update payroll entry based on StaffPayrollRequest
    public StaffPayrollResponse createOrUpdatePayroll(StaffPayrollRequest payrollRequest) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
        if (admin == null) {
            throw new CustomNotFoundException("Please login as an Admin");
        };

        User profile = userRepository.findById(payrollRequest.getStaffId())
                .orElseThrow(() -> new CustomNotFoundException("Staff not found"));

        Profile userProfile = profileRepository.findByUser(profile)
                .orElseThrow(() -> new CustomNotFoundException("Staff profile not found"));

        StaffLevelResponse staffLevel = staffLevelService.getStaffLevelById(userProfile.getStaffLevel().getId());
        if (staffLevel == null) {
            throw new CustomNotFoundException("Staff level not found");
        }

        School school = admin.getSchool();
        StaffPayroll payroll = staffPayrollRepository.findByStaffIdAndSchoolId(profile.getId(), school.getId())
                .orElse(new StaffPayroll());

        payroll.setName(admin.getFirstName() + " " + admin.getLastName());
        payroll.setUniqueRegistrationNumber(userProfile.getUniqueRegistrationNumber());
        payroll.setBonuses(payrollRequest.getBonuses());
        payroll.setDeductions(payrollRequest.getDeductions());
        payroll.setBaseSalary(staffLevel.getBaseSalary());
        payroll.setGrossPay(staffLevel.getGrossSalary());
        payroll.setNetPay(staffLevel.getNetSalary());
        payroll.setTax(staffLevel.getTax());
        payroll.setHmo(staffLevel.getHmo());
        payroll.setRemarks(payrollRequest.getRemarks());
        payroll.setStaff(userProfile);
        payroll.setSchool(school);

        // Set timestamps
        if (payroll.getCreatedAt() == null) {
            payroll.setCreatedAt(LocalDateTime.now());
        }
        payroll.setUpdatedAt(LocalDateTime.now());

        StaffPayroll savedPayroll = staffPayrollRepository.save(payroll);
        return mapToStaffPayrollResponse(savedPayroll);
    }

    // Map StaffPayroll entity to StaffPayrollResponse
    private StaffPayrollResponse mapToStaffPayrollResponse(StaffPayroll payroll) {
        return new StaffPayrollResponse(
                payroll.getId(),
                payroll.getName(),
                payroll.getUniqueRegistrationNumber(),
                payroll.getBaseSalary(),
                payroll.getBonuses(),
                payroll.getDeductions(),
                payroll.getHmo(),
                payroll.getGrossPay(),
                payroll.getTax(),
                payroll.getNetPay(),
                payroll.getDatePayed(),
                payroll.getRemarks(),
                payroll.getStaff().getId(),
                payroll.getSchool().getId(),
                payroll.getCreatedAt(),
                payroll.getUpdatedAt()
        );
    }

    // Get payroll for a specific staff member
    public StaffPayrollResponse getPayrollForStaff(Long staffId) {
        StaffPayroll payroll = staffPayrollRepository.findByStaffId(staffId)
                .orElseThrow(() -> new CustomNotFoundException("Payroll not found"));
        return mapToStaffPayrollResponse(payroll);
    }

    // Get all payroll entries
    public List<StaffPayrollResponse> getAllPayroll() {
        List<StaffPayroll> payrolls = staffPayrollRepository.findAll();
        return payrolls.stream()
                .map(this::mapToStaffPayrollResponse)
                .collect(Collectors.toList());
    }

    // Get payroll entries for the current school
    public List<StaffPayrollResponse> getPayrollForSchool() {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
        if (admin == null) {
            throw new CustomNotFoundException("Please login as an Admin");
        }
        Long schoolId = admin.getSchool().getId();

        List<StaffPayroll> payrolls = staffPayrollRepository.findBySchoolId(schoolId);
        return payrolls.stream()
                .map(this::mapToStaffPayrollResponse)
                .collect(Collectors.toList());
    }

    // Get payroll entries for a specific month and year
    public List<StaffPayrollResponse> getPayrollForMonth(int month, int year) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }

        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
        if (admin == null) {
            throw new CustomNotFoundException("Please login as an Admin");
        }

        Long schoolId = admin.getSchool().getId();

        List<StaffPayroll> payrolls = staffPayrollRepository.findBySchoolIdAndYearAndMonth(schoolId, month, year);
        return payrolls.stream()
                .map(this::mapToStaffPayrollResponse)
                .collect(Collectors.toList());
    }

    // Delete payroll entry
    public void deletePayroll(Long payrollId) {
        staffPayrollRepository.deleteById(payrollId);
    }

    public String promoteStaff(Long userId, Long staffLevelId) {
        // Fetch the Profile associated with the user
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomNotFoundException("Profile not found for userId: " + userId));

        // Fetch the new StaffLevel
        StaffLevel newStaffLevel = staffLevelRepository.findById(staffLevelId)
                .orElseThrow(() -> new CustomNotFoundException("StaffLevel not found with id: " + staffLevelId));

        // Update the staff level
        profile.setStaffLevel(newStaffLevel);

        // Save the updated profile
        profileRepository.save(profile);

        return "Staff promoted successfully";
    }
}

package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.StaffPayrollRequest;
import examination.teacherAndStudents.dto.StaffPayrollResponse;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.School;
import examination.teacherAndStudents.entity.StaffPayroll;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.repository.SchoolRepository;
import examination.teacherAndStudents.repository.StaffPayrollRepository;
import examination.teacherAndStudents.repository.UserRepository;
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

    // Create or update payroll entry based on StatePayrollRequest
    public StaffPayrollResponse createOrUpdatePayroll(StaffPayrollRequest payrollRequest) {

        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
        if (admin == null) {
            throw new CustomNotFoundException("Please login as an Admin");
        }

        Optional<User> userDetails = userRepository.findByEmail(email);
        School school = userDetails.get().getSchool();
        // Ensure staff and school exist
        User profile =   userRepository.findById(payrollRequest.getStaffId()).orElseThrow(() -> new RuntimeException("Staff not found"));
        Optional<Profile> userProfile = profileRepository.findByUser(profile);

        // Create StaffPayroll entity from StatePayrollRequest
        StaffPayroll payroll = new StaffPayroll();
        payroll.setName(userDetails.get().getFirstName() + " " + userDetails.get().getLastName());
        payroll.setUniqueRegistrationNumber(userProfile.get().getUniqueRegistrationNumber());
        payroll.setBaseSalary(payrollRequest.getBaseSalary());
        payroll.setBonuses(payrollRequest.getBonuses());
        payroll.setDeductions(payrollRequest.getDeductions());
        payroll.setHmo(payrollRequest.getHmo());
        payroll.setRemarks(payrollRequest.getRemarks());
        payroll.setStaff(userProfile.get());
        payroll.setSchool(school);

        // Calculate payroll values (gross pay, tax, etc.)
        calculatePayroll(payroll);

        // Set created and updated timestamps
        if (payroll.getId() == null) {
            payroll.setCreatedAt(LocalDateTime.now());
        }
        payroll.setUpdatedAt(LocalDateTime.now());

        // Save and return the response DTO
        StaffPayroll savedPayroll = staffPayrollRepository.save(payroll);
        return mapToStaffPayrollResponse(savedPayroll);
    }

    // Calculate gross pay, tax, deductions, etc.
    private void calculatePayroll(StaffPayroll payroll) {
        double grossPay = payroll.getBaseSalary() + payroll.getBonuses();
        double tax = grossPay * 0.05; // Example tax rate
        double deductions = payroll.getDeductions();
        double hmo = payroll.getHmo();
        double netPay = grossPay - tax - deductions - hmo;

        payroll.setGrossPay(grossPay);
        payroll.setTax(tax);
        payroll.setNetPay(netPay);
    }

    // Map StaffPayroll entity to StaffPayrollResponse
    private StaffPayrollResponse mapToStaffPayrollResponse(StaffPayroll payroll) {
        // Map entity to DTO
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
    public StaffPayrollResponse  getPayrollForStaff(Long staffId) {
        StaffPayroll payroll = staffPayrollRepository.findByStaffId(staffId).orElseThrow(() -> new RuntimeException("Payroll not found"));
        return mapToStaffPayrollResponse(payroll);
    }

//    // Get all payroll entries
    public List<StaffPayrollResponse> getAllPayroll() {
        List<StaffPayroll> payrolls = staffPayrollRepository.findAll();
        return payrolls.stream()
                .map(this::mapToStaffPayrollResponse)
                .collect(Collectors.toList());
    }

    public List<StaffPayrollResponse> getPayrollForSchool() {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
        if (admin == null) {
            throw new CustomNotFoundException("Please login as an Admin");
        }

        Optional<User> userDetails = userRepository.findByEmail(email);
        Long schoolId = userDetails.get().getSchool().getId();

        // Get all payroll entries for the specific school
        List<StaffPayroll> payrolls = staffPayrollRepository.findBySchoolId(schoolId);

        return payrolls.stream()
                .map(this::mapToStaffPayrollResponse)
                .collect(Collectors.toList());
    }


    // Get payroll entries for a specific month and year
    public List<StaffPayrollResponse> getPayrollForMonth(int month, int year) {

        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
        if (admin == null) {
            throw new CustomNotFoundException("Please login as an Admin");
        }

        Optional<User> userDetails = userRepository.findByEmail(email);

        Long schoolId = userDetails.get().getSchool().getId();

        List<StaffPayroll> payrolls = staffPayrollRepository.findBySchoolIdAndYearAndMonth(schoolId,month, year);
        return payrolls.stream()
                .map(this::mapToStaffPayrollResponse)
                .collect(Collectors.toList());
    }

    // Delete payroll entry
    public void deletePayroll(Long payrollId) {
        staffPayrollRepository.deleteById(payrollId);
    }
}

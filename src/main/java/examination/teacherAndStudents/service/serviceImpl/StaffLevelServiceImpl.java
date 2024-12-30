package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.StaffLevelRequest;
import examination.teacherAndStudents.dto.StaffLevelResponse;
import examination.teacherAndStudents.entity.StaffLevel;
import examination.teacherAndStudents.entity.StaffPayroll;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.StaffLevelRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.StaffLevelService;
import examination.teacherAndStudents.utils.Roles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StaffLevelServiceImpl implements StaffLevelService {

    @Autowired
    private StaffLevelRepository staffLevelRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public StaffLevelResponse createStaffLevel(StaffLevelRequest request) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
        if (admin == null) {
            throw new CustomNotFoundException("Please login as an Admin");
        }

        StaffLevel staffLevel = new StaffLevel();
        staffLevel.setName(request.getName());
        staffLevel.setBaseSalary(request.getBaseSalary());
        staffLevel.setHmo(request.getHmo());
        staffLevel.setSchool(admin.getSchool()); // Reusing the admin's school
        staffLevel.setGrossSalary(calculateGrossSalary(request.getBaseSalary(), request.getHmo()));
        staffLevel.setNetSalary(calculateNetSalary(staffLevel.getGrossSalary(), request.getHmo()));

        StaffLevel savedLevel = staffLevelRepository.save(staffLevel);
        return mapToStaffLevelResponse(savedLevel);
    }

    private double calculateGrossSalary(double baseSalary, double hmo) {
        return baseSalary + hmo; // Adjust this calculation as needed for gross salary.
    }

    private double calculateNetSalary(double grossSalary, double hmo) {
        double tax = grossSalary * 0.05; // Example tax rate
        return grossSalary - tax - hmo;
    }

    @Override
    public StaffLevelResponse editStaffLevel(Long id, StaffLevelRequest request) {
        StaffLevel staffLevel = staffLevelRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Staff Level not found"));

        staffLevel.setName(request.getName());
        staffLevel.setBaseSalary(request.getBaseSalary());
        staffLevel.setHmo(request.getHmo());
        staffLevel.setGrossSalary(calculateGrossSalary(request.getBaseSalary(), request.getHmo()));
        staffLevel.setNetSalary(calculateNetSalary(staffLevel.getGrossSalary(), request.getHmo()));

        StaffLevel updatedLevel = staffLevelRepository.save(staffLevel);
        return mapToStaffLevelResponse(updatedLevel);
    }

    @Override
    public void deleteStaffLevel(Long id) {
        if (!staffLevelRepository.existsById(id)) {
            throw new CustomNotFoundException("Staff Level not found");
        }
        staffLevelRepository.deleteById(id);
    }

    @Override
    public List<StaffLevelResponse> getAllStaffLevels() {
        List<StaffLevel> levels = staffLevelRepository.findAll();
        return levels.stream()
                .map(this::mapToStaffLevelResponse)
                .collect(Collectors.toList());
    }

    @Override
    public StaffLevelResponse getStaffLevelById(Long id) {
        StaffLevel staffLevel = staffLevelRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Staff Level not found"));
        return mapToStaffLevelResponse(staffLevel);
    }

    private StaffLevelResponse mapToStaffLevelResponse(StaffLevel staffLevel) {
        return new StaffLevelResponse(
                staffLevel.getId(),
                staffLevel.getName(),
                staffLevel.getGrossSalary(),
                staffLevel.getBaseSalary(),
                staffLevel.getHmo(),
                staffLevel.getNetSalary(),
                staffLevel.getGrossSalary() * 0.05 // Calculate tax on the fly if needed
        );
    }
}

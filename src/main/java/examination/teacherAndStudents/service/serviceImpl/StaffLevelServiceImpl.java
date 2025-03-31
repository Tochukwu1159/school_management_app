package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.StaffLevelRequest;
import examination.teacherAndStudents.dto.StaffLevelResponse;
import examination.teacherAndStudents.entity.School;
import examination.teacherAndStudents.entity.StaffLevel;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.StaffLevelRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.StaffLevelService;
import examination.teacherAndStudents.utils.Roles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StaffLevelServiceImpl implements StaffLevelService {

    private static final String ADMIN_NOT_FOUND = "Please login as an Admin";
    private static final String STAFF_LEVEL_NOT_FOUND = "Staff Level not found with ID: ";
    private static final double TAX_RATE = 0.05;

    private final StaffLevelRepository staffLevelRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public StaffLevelResponse createStaffLevel(StaffLevelRequest request) {
        log.info("Creating new staff level: {}", request.getName());

        User admin = getAuthenticatedAdmin();
        validateStaffLevelRequest(request);

        StaffLevel staffLevel = buildStaffLevel(request, admin.getSchool());
        StaffLevel savedLevel = staffLevelRepository.save(staffLevel);

        log.info("Created staff level with ID: {}", savedLevel.getId());
        return mapToStaffLevelResponse(savedLevel);
    }

    @Override
    @Transactional
    public StaffLevelResponse editStaffLevel(Long id, StaffLevelRequest request) {
        log.info("Updating staff level with ID: {}", id);

        StaffLevel staffLevel = findStaffLevelById(id);
        validateStaffLevelRequest(request);

        updateStaffLevelDetails(staffLevel, request);
        StaffLevel updatedLevel = staffLevelRepository.save(staffLevel);

        return mapToStaffLevelResponse(updatedLevel);
    }

    @Override
    @Transactional
    public void deleteStaffLevel(Long id) {
        log.info("Deleting staff level with ID: {}", id);

        if (!staffLevelRepository.existsById(id)) {
            throw new CustomNotFoundException(STAFF_LEVEL_NOT_FOUND + id);
        }
        staffLevelRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffLevelResponse> getAllStaffLevels() {
        log.info("Fetching all staff levels");
        return staffLevelRepository.findAll().stream()
                .map(this::mapToStaffLevelResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StaffLevelResponse getStaffLevelById(Long id) {
        log.info("Fetching staff level with ID: {}", id);
        return mapToStaffLevelResponse(findStaffLevelById(id));
    }

    // Helper methods
    private User getAuthenticatedAdmin() {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        return userRepository.findByEmailAndRoles(email, Roles.ADMIN)
                .orElseThrow(() -> new CustomNotFoundException(ADMIN_NOT_FOUND));
    }

    private StaffLevel findStaffLevelById(Long id) {
        return staffLevelRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException(STAFF_LEVEL_NOT_FOUND + id));
    }

    private void validateStaffLevelRequest(StaffLevelRequest request) {
        if (request.getBaseSalary() <= 0) {
            throw new IllegalArgumentException("Base salary must be positive");
        }
        if (request.getHmo() < 0) {
            throw new IllegalArgumentException("HMO contribution cannot be negative");
        }
    }

    private StaffLevel buildStaffLevel(StaffLevelRequest request, School school) {
        double grossSalary = calculateGrossSalary(request.getBaseSalary(), request.getHmo());
        double netSalary = calculateNetSalary(grossSalary, request.getHmo());

        return StaffLevel.builder()
                .name(request.getName())
                .baseSalary(request.getBaseSalary())
                .hmo(request.getHmo())
                .school(school)
                .grossSalary(grossSalary)
                .netSalary(netSalary)
                .build();
    }

    private void updateStaffLevelDetails(StaffLevel staffLevel, StaffLevelRequest request) {
        double grossSalary = calculateGrossSalary(request.getBaseSalary(), request.getHmo());
        double netSalary = calculateNetSalary(grossSalary, request.getHmo());

        staffLevel.setName(request.getName());
        staffLevel.setBaseSalary(request.getBaseSalary());
        staffLevel.setHmo(request.getHmo());
        staffLevel.setGrossSalary(grossSalary);
        staffLevel.setNetSalary(netSalary);
    }

    private double calculateGrossSalary(double baseSalary, double hmo) {
        return baseSalary + hmo;
    }

    private double calculateNetSalary(double grossSalary, double hmo) {
        double tax = grossSalary * TAX_RATE;
        return grossSalary - tax - hmo;
    }

    private StaffLevelResponse mapToStaffLevelResponse(StaffLevel staffLevel) {
        return StaffLevelResponse.builder()
                .id(staffLevel.getId())
                .name(staffLevel.getName())
                .grossSalary(staffLevel.getGrossSalary())
                .baseSalary(staffLevel.getBaseSalary())
                .hmo(staffLevel.getHmo())
                .netSalary(staffLevel.getNetSalary())
                .tax(staffLevel.getGrossSalary() * TAX_RATE)
                .build();
    }
}
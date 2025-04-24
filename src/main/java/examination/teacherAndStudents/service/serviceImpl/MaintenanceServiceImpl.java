package examination.teacherAndStudents.service.serviceImpl;
import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.MaintenanceRequest;
import examination.teacherAndStudents.dto.MaintenanceResponse;
import examination.teacherAndStudents.entity.Maintenance;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.Bus;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.MaintenanceRepository;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.repository.TransportRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.MaintenanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MaintenanceServiceImpl implements MaintenanceService {

    private final MaintenanceRepository maintenanceRepository;
    private final TransportRepository transportRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    public MaintenanceResponse createMaintenance(MaintenanceRequest request) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        Optional<User> repairer = userRepository.findByEmail(email);

        Profile repairerProfile = profileRepository.findByUser(repairer.get())
                .orElseThrow(() -> new CustomNotFoundException("Transport not found with ID: " + request.getTransportId()));

        Bus transport = transportRepository.findById(request.getTransportId())
                .orElseThrow(() -> new CustomNotFoundException("Transport not found with ID: " + request.getTransportId()));

        Maintenance maintenance = Maintenance.builder()
                .description(request.getDescription())
                .amountSpent(request.getAmountSpent())
                .maintainedBy(repairerProfile)
                .transport(transport)
                .build();
        Maintenance savedMaintenance = maintenanceRepository.save(maintenance);
        return toResponse(savedMaintenance);
    }

    public MaintenanceResponse updateMaintenance(Long id, MaintenanceRequest request) {
        Maintenance maintenance = maintenanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Maintenance not found"));

        Bus transport = transportRepository.findById(request.getTransportId())
                .orElseThrow(() -> new CustomNotFoundException("Transport not found with ID: " + request.getTransportId()));

        maintenance.setDescription(request.getDescription());
        maintenance.setAmountSpent(request.getAmountSpent());
        maintenance.setTransport(transport);
        maintenanceRepository.save(maintenance);
        return toResponse(maintenance);
    }

    public void deleteMaintenance(Long id) {
        maintenanceRepository.deleteById(id);
    }

    public Page<MaintenanceResponse> getAllMaintenances(
            Long id,
            Long transportId,
            Long maintainedById,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            Profile user = profileRepository.findByUserEmail(email)
                    .orElseThrow(() -> new CustomNotFoundException("User not found"));

            // Create Pageable object
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            // Fetch filtered maintenance records
            Page<Maintenance> maintenancesPage = maintenanceRepository.findAllBySchoolWithFilters(
                    user.getUser().getSchool().getId(),
                    id,
                    transportId,
                    maintainedById,
                    startDate,
                    endDate,
                    pageable);

            // Map to response DTO
            return maintenancesPage.map(this::mapToMaintenanceResponse);
        } catch (CustomNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomInternalServerException("Error fetching maintenance records: " + e.getMessage());
        }
    }

    private MaintenanceResponse mapToMaintenanceResponse(Maintenance maintenance) {
        return MaintenanceResponse.builder()
                .id(maintenance.getId())
                .description(maintenance.getDescription())
                .amountSpent(maintenance.getAmountSpent())
                .transportId(maintenance.getTransport().getBusId())
                .transportVehicleNumber(maintenance.getTransport().getVehicleNumber())
                .maintainedById(maintenance.getMaintainedBy().getId())
                .maintainedByName(maintenance.getMaintainedBy().getUser().getFirstName() + " " + maintenance.getMaintainedBy().getUser().getLastName())
                .maintenanceDate(maintenance.getMaintenanceDate())
                .updatedAt(maintenance.getUpdatedAt())
                .build();
    }

    public MaintenanceResponse getMaintenanceById(Long id) {
        Maintenance maintenance = maintenanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Maintenance not found"));
        return toResponse(maintenance);
    }

    private MaintenanceResponse toResponse(Maintenance maintenance) {
        return MaintenanceResponse.builder()
                .id(maintenance.getId())
                .description(maintenance.getDescription())
                .amountSpent(maintenance.getAmountSpent())
                .maintenanceDate(maintenance.getMaintenanceDate())
                .updatedAt(maintenance.getUpdatedAt())
                .build();
    }
}

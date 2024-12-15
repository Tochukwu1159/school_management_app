package examination.teacherAndStudents.service.serviceImpl;
import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.MaintenanceRequest;
import examination.teacherAndStudents.dto.MaintenanceResponse;
import examination.teacherAndStudents.entity.Maintenance;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.Transport;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.MaintenanceRepository;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.repository.TransportRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.MaintenanceService;
import examination.teacherAndStudents.utils.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

        Transport transport = transportRepository.findById(request.getTransportId())
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

        Transport transport = transportRepository.findById(request.getTransportId())
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

    public List<MaintenanceResponse> getAllMaintenances() {
        return maintenanceRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
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

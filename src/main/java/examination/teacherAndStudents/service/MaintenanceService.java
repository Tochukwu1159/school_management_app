package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.MaintenanceRequest;
import examination.teacherAndStudents.dto.MaintenanceResponse;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public interface MaintenanceService {
    MaintenanceResponse getMaintenanceById(Long id);
    Page<MaintenanceResponse> getAllMaintenances(
            Long id,
            Long transportId,
            Long maintainedById,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int size,
            String sortBy,
            String sortDirection);
    MaintenanceResponse updateMaintenance(Long id, MaintenanceRequest request);
    MaintenanceResponse createMaintenance(MaintenanceRequest request);
    void deleteMaintenance(Long id);
}

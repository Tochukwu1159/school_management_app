package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.MaintenanceRequest;
import examination.teacherAndStudents.dto.MaintenanceResponse;

import java.util.List;

public interface MaintenanceService {
    MaintenanceResponse getMaintenanceById(Long id);
    List<MaintenanceResponse> getAllMaintenances();
    MaintenanceResponse updateMaintenance(Long id, MaintenanceRequest request);
    MaintenanceResponse createMaintenance(MaintenanceRequest request);
    void deleteMaintenance(Long id);
}

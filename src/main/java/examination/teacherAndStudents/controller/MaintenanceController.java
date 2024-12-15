
package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.MaintenanceRequest;
import examination.teacherAndStudents.dto.MaintenanceResponse;
import examination.teacherAndStudents.service.MaintenanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance")
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    @PostMapping
    public ResponseEntity<MaintenanceResponse> createMaintenance(@RequestBody MaintenanceRequest request) {
        return ResponseEntity.ok(maintenanceService.createMaintenance(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MaintenanceResponse> updateMaintenance(@PathVariable Long id, @RequestBody MaintenanceRequest request) {
        return ResponseEntity.ok(maintenanceService.updateMaintenance(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaintenance(@PathVariable Long id) {
        maintenanceService.deleteMaintenance(id);
        return ResponseEntity.noContent().build();
    }@GetMapping
    public ResponseEntity<List<MaintenanceResponse>> getAllMaintenances() {
        return ResponseEntity.ok(maintenanceService.getAllMaintenances());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceResponse> getMaintenanceById(@PathVariable Long id) {
        return ResponseEntity.ok(maintenanceService.getMaintenanceById(id));
    }
}
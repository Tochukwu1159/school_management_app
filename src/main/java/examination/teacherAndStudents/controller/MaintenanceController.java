
package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.MaintenanceRequest;
import examination.teacherAndStudents.dto.MaintenanceResponse;
import examination.teacherAndStudents.service.MaintenanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
    }

    @GetMapping
    public ResponseEntity<Page<MaintenanceResponse>> getAllMaintenances(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) Long transportId,
            @RequestParam(required = false) Long maintainedById,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "maintenanceDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Page<MaintenanceResponse> response = maintenanceService.getAllMaintenances(
                id,
                transportId,
                maintainedById,
                startDate,
                endDate,
                page,
                size,
                sortBy,
                sortDirection);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceResponse> getMaintenanceById(@PathVariable Long id) {
        return ResponseEntity.ok(maintenanceService.getMaintenanceById(id));
    }
}
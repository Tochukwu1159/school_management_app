package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.MaintenanceRequest;
import examination.teacherAndStudents.dto.MaintenanceResponse;
import examination.teacherAndStudents.service.MaintenanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/maintenance")
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    @PostMapping
    public ResponseEntity<ApiResponse<MaintenanceResponse>> createMaintenance(@RequestBody MaintenanceRequest request) {
        MaintenanceResponse response = maintenanceService.createMaintenance(request);
        ApiResponse<MaintenanceResponse> apiResponse = new ApiResponse<>("Maintenance created successfully", true, response);
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MaintenanceResponse>> updateMaintenance(@PathVariable Long id, @RequestBody MaintenanceRequest request) {
        MaintenanceResponse response = maintenanceService.updateMaintenance(id, request);
        ApiResponse<MaintenanceResponse> apiResponse = new ApiResponse<>("Maintenance updated successfully", true, response);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteMaintenance(@PathVariable Long id) {
        maintenanceService.deleteMaintenance(id);
        ApiResponse<String> apiResponse = new ApiResponse<>("Maintenance deleted successfully", true, null);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Page<ApiResponse<MaintenanceResponse>>> getAllMaintenances(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) Long transportId,
            @RequestParam(required = false) Long maintainedById,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "maintenanceDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Page<MaintenanceResponse> responsePage = maintenanceService.getAllMaintenances(
                id,
                transportId,
                maintainedById,
                startDate,
                endDate,
                page,
                size,
                sortBy,
                sortDirection);

        Page<ApiResponse<MaintenanceResponse>> apiResponsePage = new PageImpl<>(
                responsePage.getContent().stream()
                        .map(maintenance -> new ApiResponse<>("Maintenance fetched successfully", true, maintenance))
                        .collect(Collectors.toList()),
                responsePage.getPageable(),
                responsePage.getTotalElements()
        );

        return ResponseEntity.ok(apiResponsePage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MaintenanceResponse>> getMaintenanceById(@PathVariable Long id) {
        MaintenanceResponse response = maintenanceService.getMaintenanceById(id);
        ApiResponse<MaintenanceResponse> apiResponse = new ApiResponse<>("Maintenance fetched successfully", true, response);
        return ResponseEntity.ok(apiResponse);
    }
}

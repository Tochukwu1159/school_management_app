package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.ServiceOfferedDTO;
import examination.teacherAndStudents.entity.ServiceOffered;
import examination.teacherAndStudents.service.ServiceOfferedService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing services offered.
 */
@RestController
@RequestMapping("/api/v1/services-offered")
@RequiredArgsConstructor
public class ServiceOfferedController {

    private final ServiceOfferedService serviceOfferedService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ServiceOfferedDTO>> createServiceOffered(@Valid @RequestBody ServiceOfferedDTO dto) {
        ServiceOffered service = serviceOfferedService.createServiceOffered(dto.getName(), dto.getIsDefault());
        ServiceOfferedDTO responseDTO = mapToDTO(service);
        return ResponseEntity.status(201).body(new ApiResponse<>("Service created successfully", true, responseDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ServiceOfferedDTO>> updateServiceOffered(
            @PathVariable Long id, @Valid @RequestBody ServiceOfferedDTO dto) {
        ServiceOffered service = serviceOfferedService.updateServiceOffered(id, dto.getName(), dto.getIsDefault());
        return ResponseEntity.ok(new ApiResponse<>("Service updated successfully", true, mapToDTO(service)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteServiceOffered(@PathVariable Long id) {
        serviceOfferedService.deleteServiceOffered(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceOfferedDTO>> getServiceOfferedById(@PathVariable Long id) {
        ServiceOffered service = serviceOfferedService.getServiceOfferedById(id);
        return ResponseEntity.ok(new ApiResponse<>("Service found successfully", true, mapToDTO(service)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ServiceOfferedDTO>>> getAllServicesOffered(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean isDefault,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ServiceOffered> services = serviceOfferedService.getAllServicesOffered(name, isDefault, pageable);
        Page<ServiceOfferedDTO> responsePage = services.map(this::mapToDTO);
        return ResponseEntity.ok(new ApiResponse<>("List of services retrieved successfully", true, responsePage));
    }

    private ServiceOfferedDTO mapToDTO(ServiceOffered service) {
        return ServiceOfferedDTO.builder()
                .id(service.getId())
                .name(service.getName())
                .isDefault(service.isDefault())
                .build();
    }
}

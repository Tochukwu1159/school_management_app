package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.FeeCategoryDTO;
import examination.teacherAndStudents.entity.FeeCategory;
import examination.teacherAndStudents.service.FeeCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for managing fee categories.
 */
@RestController
@RequestMapping("/api/v1/fee-categories")
@RequiredArgsConstructor
public class FeeCategoryController {

    private final FeeCategoryService feeCategoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FeeCategoryDTO>> createFeeCategory(@Valid @RequestBody FeeCategoryDTO dto) {
        FeeCategory category = feeCategoryService.createFeeCategory(dto.getName());
        FeeCategoryDTO responseDTO = mapToDTO(category);
        return ResponseEntity.created(URI.create("/api/v1/fee-categories/" + category.getId()))
                .body(new ApiResponse<>("Fee category created successfully", true, responseDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FeeCategoryDTO>> updateFeeCategory(
            @PathVariable Long id, @Valid @RequestBody FeeCategoryDTO dto) {
        FeeCategory category = feeCategoryService.updateFeeCategory(id, dto.getName());
        FeeCategoryDTO responseDTO = mapToDTO(category);
        return ResponseEntity.ok(new ApiResponse<>("Fee category updated successfully", true, responseDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteFeeCategory(@PathVariable Long id) {
        feeCategoryService.deleteFeeCategory(id);
        return ResponseEntity.ok(new ApiResponse<>("Fee category deleted successfully", true));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FeeCategoryDTO>> getFeeCategoryById(@PathVariable Long id) {
        FeeCategory category = feeCategoryService.getFeeCategoryById(id);
        FeeCategoryDTO responseDTO = mapToDTO(category);
        return ResponseEntity.ok(new ApiResponse<>("Fee category retrieved successfully", true, responseDTO));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<FeeCategoryDTO>>> getAllFeeCategories(Pageable pageable) {
        Page<FeeCategory> categories = feeCategoryService.getAllFeeCategories(pageable);
        Page<FeeCategoryDTO> responsePage = categories.map(this::mapToDTO);
        return ResponseEntity.ok(new ApiResponse<>("Fee categories retrieved successfully", true, responsePage));
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<FeeCategoryDTO>>> getAllFeeCategoriesList() {
        List<FeeCategory> categories = feeCategoryService.getAllFeeCategories();
        List<FeeCategoryDTO> responseList = categories.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>("Fee categories list retrieved successfully", true, responseList));
    }

    private FeeCategoryDTO mapToDTO(FeeCategory category) {
        return FeeCategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
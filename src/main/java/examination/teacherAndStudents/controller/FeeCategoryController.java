package examination.teacherAndStudents.controller;

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
    public ResponseEntity<FeeCategoryDTO> createFeeCategory(@Valid @RequestBody FeeCategoryDTO dto) {
        FeeCategory category = feeCategoryService.createFeeCategory(dto.getName());
        FeeCategoryDTO responseDTO = mapToDTO(category);
        return ResponseEntity.created(URI.create("/api/v1/fee-categories/" + category.getId()))
                .body(responseDTO);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FeeCategoryDTO> updateFeeCategory(
            @PathVariable Long id, @Valid @RequestBody FeeCategoryDTO dto) {
        FeeCategory category = feeCategoryService.updateFeeCategory(id, dto.getName());
        return ResponseEntity.ok(mapToDTO(category));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteFeeCategory(@PathVariable Long id) {
        feeCategoryService.deleteFeeCategory(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{id}")
    public ResponseEntity<FeeCategoryDTO> getFeeCategoryById(@PathVariable Long id) {
        FeeCategory category = feeCategoryService.getFeeCategoryById(id);
        return ResponseEntity.ok(mapToDTO(category));
    }

    @GetMapping
    public ResponseEntity<Page<FeeCategoryDTO>> getAllFeeCategories(Pageable pageable) {
        Page<FeeCategory> categories = feeCategoryService.getAllFeeCategories(pageable);
        Page<FeeCategoryDTO> responsePage = categories.map(this::mapToDTO);
        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/list")
    public ResponseEntity<List<FeeCategoryDTO>> getAllFeeCategoriesList() {
        List<FeeCategory> categories = feeCategoryService.getAllFeeCategories();
        List<FeeCategoryDTO> responseList = categories.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseList);
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
package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.CategoryRequest;
import examination.teacherAndStudents.dto.CategoryResponse;
import examination.teacherAndStudents.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/store/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(201).body(new ApiResponse<>("Category created successfully", true, response));
    }

    @PutMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> editCategory(
            @PathVariable @NotNull Long categoryId,
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.editCategory(categoryId, request);
        return ResponseEntity.ok(new ApiResponse<>("Category updated successfully", true, response));
    }

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable @NotNull Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok(new ApiResponse<>("Category deleted successfully", true));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable @NotNull Long categoryId) {
        CategoryResponse response = categoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(new ApiResponse<>("Category retrieved successfully", true, response));
    }

    @GetMapping("/school/{schoolId}")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategoriesForSchool(@PathVariable @NotNull Long schoolId) {
        List<CategoryResponse> responses = categoryService.getAllCategoriesForSchool(schoolId);
        return ResponseEntity.ok(new ApiResponse<>("Categories retrieved successfully", true, responses));
    }
}
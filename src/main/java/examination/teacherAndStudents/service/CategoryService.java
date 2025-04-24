package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.CategoryRequest;
import examination.teacherAndStudents.dto.CategoryResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public interface CategoryService {

    CategoryResponse createCategory(@Valid CategoryRequest request);

    CategoryResponse editCategory(@NotNull Long categoryId, @Valid CategoryRequest request);

    void deleteCategory(@NotNull Long categoryId);

    CategoryResponse getCategoryById(@NotNull Long categoryId);

    List<CategoryResponse> getAllCategoriesForSchool(@NotNull Long schoolId); }
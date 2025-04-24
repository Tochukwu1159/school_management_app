package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.CategoryRequest;
import examination.teacherAndStudents.dto.CategoryResponse;
import examination.teacherAndStudents.entity.Category;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.UnauthorizedException;
import examination.teacherAndStudents.repository.CategoryRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.CategoryService;
import examination.teacherAndStudents.utils.Roles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CategoryResponse createCategory(@Valid CategoryRequest request) {
        User admin = validateAdminUser();

        Category category = Category.builder()
                .name(request.getName())
                .build();

        Category savedCategory = categoryRepository.save(category);
        log.info("Category created [categoryId={}, name={}]", savedCategory.getId(), savedCategory.getName());

        return mapToResponse(savedCategory);
    }

    @Override
    @Transactional
    public CategoryResponse editCategory(@NotNull Long categoryId, @Valid CategoryRequest request) {
        User admin = validateAdminUser();
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomNotFoundException("Category not found with ID: " + categoryId));

        category.setName(request.getName());
        Category updatedCategory = categoryRepository.save(category);
        log.info("Category updated [categoryId={}, name={}]", updatedCategory.getId(), updatedCategory.getName());

        return mapToResponse(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(@NotNull Long categoryId) {
        User admin = validateAdminUser();
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomNotFoundException("Category not found with ID: " + categoryId));

        categoryRepository.delete(category);
        log.info("Category deleted [categoryId={}]", categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(@NotNull Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomNotFoundException("Category not found with ID: " + categoryId));

        log.debug("Category retrieved [categoryId={}]", categoryId);
        return mapToResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategoriesForSchool(@NotNull Long schoolId) {
        // No school-specific filtering since categories are global
        List<Category> categories = categoryRepository.findAll();
        log.debug("Retrieved {} categories", categories.size());

        return categories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private User validateAdminUser() {
        String email = getAuthenticatedUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("User not found with email: " + email));

        if (!user.getRoles().contains(Roles.ADMIN)) {
            throw new UnauthorizedException("Please login as an Admin");
        }

        return user;
    }

    private String getAuthenticatedUserEmail() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            log.error("Failed to retrieve authenticated user email", e);
            throw new UnauthorizedException("Unable to authenticate user");
        }
    }

    private CategoryResponse mapToResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName()
        );
    }
}
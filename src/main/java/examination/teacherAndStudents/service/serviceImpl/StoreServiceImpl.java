package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.StoreRequest;
import examination.teacherAndStudents.dto.StoreResponse;
import examination.teacherAndStudents.entity.Category;
import examination.teacherAndStudents.entity.School;
import examination.teacherAndStudents.entity.Store;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.UnauthorizedException;
import examination.teacherAndStudents.repository.CategoryRepository;
import examination.teacherAndStudents.repository.SchoolRepository;
import examination.teacherAndStudents.repository.StoreRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.StoreService;
import examination.teacherAndStudents.utils.Roles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public StoreResponse createStore(@Valid StoreRequest request) {
        User admin = validateAdminUser();
        School school = validateSchool(admin.getSchool().getId());
        Set<Category> categories = validateAndFetchCategories(request.getCategoryIds());

        Store store = Store.builder()
                .name(request.getName())
                .school(school)
                .categories(categories)
                .build();

        Store savedStore = storeRepository.save(store);
        log.info("Store created [storeId={}, name={}, schoolId={}]", savedStore.getId(), savedStore.getName(), school.getId());

        return mapToResponse(savedStore);
    }

    @Override
    @Transactional
    public StoreResponse editStore(@NotNull Long storeId, @Valid StoreRequest request) {
        User admin = validateAdminUser();
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomNotFoundException("Store not found with ID: " + storeId));

        validateStoreOwnership(admin, store);
        Set<Category> categories = validateAndFetchCategories(request.getCategoryIds());

        store.setName(request.getName());
        store.setCategories(categories);
        Store updatedStore = storeRepository.save(store);
        log.info("Store updated [storeId={}, name={}]", updatedStore.getId(), updatedStore.getName());

        return mapToResponse(updatedStore);
    }

    @Override
    @Transactional
    public void deleteStore(@NotNull Long storeId) {
        User admin = validateAdminUser();
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomNotFoundException("Store not found with ID: " + storeId));

        validateStoreOwnership(admin, store);

        storeRepository.delete(store);
        log.info("Store deleted [storeId={}]", storeId);
    }

    @Override
    @Transactional(readOnly = true)
    public StoreResponse getStoreById(@NotNull Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomNotFoundException("Store not found with ID: " + storeId));

        validateUserAccess(store);
        log.debug("Store retrieved [storeId={}]", storeId);

        return mapToResponse(store);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoreResponse> getAllStoresForSchool(@NotNull Long schoolId) {
        User admin = validateAdminUser();
        School school = validateSchool(schoolId);

        if (!admin.getSchool().getId().equals(schoolId)) {
            throw new UnauthorizedException("You do not have access to this school's stores");
        }

        List<Store> stores = storeRepository.findBySchoolId(schoolId);
        log.debug("Retrieved {} stores for school [schoolId={}]", stores.size(), schoolId);

        return stores.stream()
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

    private School validateSchool(Long schoolId) {
        return schoolRepository.findById(schoolId)
                .orElseThrow(() -> new CustomNotFoundException("School not found with ID: " + schoolId));
    }

    private Set<Category> validateAndFetchCategories(Set<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return new HashSet<>();
        }

        Set<Category> categories = new HashSet<>(categoryRepository.findAllById(categoryIds));
        if (categories.size() != categoryIds.size()) {
            throw new CustomNotFoundException("One or more categories not found");
        }

        return categories;
    }

    private void validateStoreOwnership(User admin, Store store) {
        if (!store.getSchool().getId().equals(admin.getSchool().getId())) {
            throw new UnauthorizedException("You do not have permission to modify this store");
        }
    }

    private void validateUserAccess(Store store) {
        User user = validateAdminUser();
        if (!store.getSchool().getId().equals(user.getSchool().getId())) {
            throw new UnauthorizedException("You do not have access to this store");
        }
    }

    private StoreResponse mapToResponse(Store store) {
        return new StoreResponse(
                store.getId(),
                store.getName(),
                store.getSchool().getId(),
                store.getCategories().stream().map(Category::getId).collect(Collectors.toSet())
        );
    }
}
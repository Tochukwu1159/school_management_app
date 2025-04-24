package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.StoreItemRequest;
import examination.teacherAndStudents.dto.StoreItemResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.UnauthorizedException;
import examination.teacherAndStudents.repository.CategoryRepository;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.repository.StoreItemRepository;
import examination.teacherAndStudents.repository.StoreRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.StoreItemService;
import examination.teacherAndStudents.utils.Roles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreItemServiceImpl implements StoreItemService {

    private final StoreItemRepository storeItemRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public StoreItemResponse createStoreItem(@Valid StoreItemRequest request) {
        request.validate(); // Validate sizes or quantity
        User admin = validateAdminUser();
        Profile profile = getAdminProfile(admin);
        Store store = getStore(request.getStoreId());
        Category category = getCategory(request.getCategoryId());

        validateStoreOwnership(admin, store);
        validateStoreCategory(store, category);

        StoreItem storeItem = StoreItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .photoUrl(request.getPhoto())
                .sizes(request.getSizes())
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .store(store)
                .school(admin.getSchool())
                .category(category)
                .build();

        StoreItem savedItem = storeItemRepository.save(storeItem);
        log.info("Store item created [itemId={}, name={}, storeId={}, categoryId={}]",
                savedItem.getId(), savedItem.getName(), store.getId(), category.getId());

        return StoreItemResponse.from(savedItem);
    }

    @Override
    @Transactional
    public StoreItemResponse editStoreItem(Long itemId, @Valid StoreItemRequest request) {
        request.validate(); // Validate sizes or quantity
        User admin = validateAdminUser();
        StoreItem storeItem = getStoreItemByIdAndSchool(itemId, admin.getSchool());
        Store store = getStore(request.getStoreId());
        Category category = getCategory(request.getCategoryId());

        validateStoreOwnership(admin, store);
        validateStoreCategory(store, category);

        storeItem.updateDetails(
                request.getName(),
                request.getDescription(),
                request.getPhoto(),
                request.getSizes(),
                request.getQuantity(),
                request.getPrice(),
                category
        );
        storeItem.setStore(store);

        StoreItem updatedItem = storeItemRepository.save(storeItem);
        log.info("Store item updated [itemId={}, name={}, storeId={}, categoryId={}]",
                updatedItem.getId(), updatedItem.getName(), store.getId(), category.getId());

        return StoreItemResponse.from(updatedItem);
    }

    @Override
    @Transactional
    public void deleteStoreItem(Long itemId) {
        User admin = validateAdminUser();
        StoreItem storeItem = getStoreItemByIdAndSchool(itemId, admin.getSchool());
        validateStoreOwnership(admin, storeItem.getStore());

        storeItemRepository.delete(storeItem);
        log.info("Store item deleted [itemId={}]", itemId);
    }

    @Override
    @Transactional(readOnly = true)
    public StoreItemResponse getStoreItemById(Long itemId) {
        User admin = validateAdminUser();
        StoreItem storeItem = getStoreItemByIdAndSchool(itemId, admin.getSchool());
        log.debug("Store item retrieved [itemId={}]", itemId);
        return StoreItemResponse.from(storeItem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoreItemResponse> getAllStoreItemsForSchool() {
        User admin = validateAdminUser();
        List<StoreItem> items = storeItemRepository.findBySchoolId(admin.getSchool().getId());
        log.debug("Retrieved {} store items for school [schoolId={}]", items.size(), admin.getSchool().getId());
        return items.stream()
                .map(StoreItemResponse::from)
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

    private Profile getAdminProfile(User admin) {
        return profileRepository.findByUser(admin)
                .orElseThrow(() -> new CustomNotFoundException("Profile not found for user: " + admin.getEmail()));
    }

    private Store getStore(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomNotFoundException("Store not found with ID: " + storeId));
    }

    private Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomNotFoundException("Category not found with ID: " + categoryId));
    }

    private StoreItem getStoreItemByIdAndSchool(Long itemId, School school) {
        return storeItemRepository.findByIdAndSchoolId(itemId, school.getId())
                .orElseThrow(() -> new CustomNotFoundException("Store item not found with ID: " + itemId));
    }

    private void validateStoreOwnership(User admin, Store store) {
        if (!store.getSchool().getId().equals(admin.getSchool().getId())) {
            throw new UnauthorizedException("You do not have permission to modify this store");
        }
    }

    private void validateStoreCategory(Store store, Category category) {
        if (!store.getCategories().contains(category)) {
            throw new IllegalArgumentException("Category ID " + category.getId() + " is not associated with store ID " + store.getId());
        }
    }
}
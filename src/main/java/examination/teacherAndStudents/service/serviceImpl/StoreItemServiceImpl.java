package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.StoreItemRequest;
import examination.teacherAndStudents.dto.StoreItemResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.AuthenticationFailedException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.repository.StoreItemRepository;
import examination.teacherAndStudents.repository.StoreRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.StoreItemService;
import examination.teacherAndStudents.utils.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.stream.Collectors;
@Service
@Transactional
@RequiredArgsConstructor
public class StoreItemServiceImpl implements StoreItemService {
    private final StoreItemRepository storeItemRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    @Override
    @Transactional
    public StoreItemResponse createStoreItem(StoreItemRequest request) {
        User admin = getAuthenticatedAdmin();
        Profile profile = getAdminProfile(admin);
        Store store = getStore(request.getStoreId());

        StoreItem storeItem = StoreItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .photoUrl(request.getPhoto())
                .sizes(request.getSizes())
                .price(request.getPrice())
                .store(store)
                .school(admin.getSchool())
                .build();

        return StoreItemResponse.from(storeItemRepository.save(storeItem));
    }

    @Override
    @Transactional
    public StoreItemResponse editStoreItem(Long itemId, StoreItemRequest request) {
        User admin = getAuthenticatedAdmin();
        StoreItem storeItem = getStoreItemByIdAndSchool(itemId, admin.getSchool());

        storeItem.updateDetails(
                request.getName(),
                request.getDescription(),
                request.getPhoto(),
                request.getSizes(),
                request.getPrice()
        );

        return StoreItemResponse.from(storeItemRepository.save(storeItem));
    }

    @Override
    @Transactional
    public void deleteStoreItem(Long itemId) {
        User admin = getAuthenticatedAdmin();
        StoreItem storeItem = getStoreItemByIdAndSchool(itemId, admin.getSchool());
        storeItemRepository.delete(storeItem);
    }

    @Override
    @Transactional(readOnly = true)
    public StoreItemResponse getStoreItemById(Long itemId) {
        User admin = getAuthenticatedAdmin();
        return StoreItemResponse.from(getStoreItemByIdAndSchool(itemId, admin.getSchool()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoreItemResponse> getAllStoreItemsForSchool() {
        User admin = getAuthenticatedAdmin();
        return storeItemRepository.findBySchoolId(admin.getSchool().getId()).stream()
                .map(StoreItemResponse::from)
                .collect(Collectors.toList());
    }

    // Helper methods
    private User getAuthenticatedAdmin() {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        return userRepository.findByEmailAndRoles(email, Roles.ADMIN)
                .orElseThrow(() -> new AuthenticationFailedException("User not found or not authenticated"));
    }

    private Profile getAdminProfile(User admin) {
        return profileRepository.findByUser(admin)
                .orElseThrow(() -> new NotFoundException("Profile not found"));
    }

    private Store getStore(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new NotFoundException("Store"));
    }

    private StoreItem getStoreItemByIdAndSchool(Long itemId, School school) {
        return storeItemRepository.findByIdAndSchoolId(itemId, school.getId())
                .orElseThrow(() -> new NotFoundException("Store item not found"));
    }
}
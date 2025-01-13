package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.StoreRequest;
import examination.teacherAndStudents.dto.StoreResponse;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.School;
import examination.teacherAndStudents.entity.StoreItem;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.repository.SchoolRepository;
import examination.teacherAndStudents.repository.StoreRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.StoreService;
import examination.teacherAndStudents.utils.Roles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.stream.Collectors;
@Service
public class StoreServiceImpl implements StoreService {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private SchoolRepository schoolRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProfileRepository profileRepository;

    // Create a store
    public StoreResponse createStore(StoreRequest request) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
        if (admin == null) {
            throw new CustomNotFoundException("Please login as an Admin");
        };

        Profile profile = profileRepository.findByUser(admin)
                .orElseThrow(() -> new CustomNotFoundException("Staff not found"));

        StoreItem storeItem = new StoreItem();
        storeItem.setName(request.getName());
        storeItem.setDescription(request.getDescription());
        storeItem.setPhoto(request.getPhoto());
        storeItem.setSizes(request.getSizes());
        storeItem.setPrice(request.getPrice());
        storeItem.setSchool(profile.getUser().getSchool());

        storeItem = storeRepository.save(storeItem);

        return mapToResponse(storeItem);
    }

    // Edit a store
    public StoreResponse editStore(Long storeId, StoreRequest request) {
        StoreItem storeItem = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        storeItem.setName(request.getName());
        storeItem.setDescription(request.getDescription());
        storeItem.setPhoto(request.getPhoto());
        storeItem.setSizes(request.getSizes());
        storeItem.setPrice(request.getPrice());

        storeItem = storeRepository.save(storeItem);

        return mapToResponse(storeItem);
    }

    // Delete a store
    public void deleteStore(Long storeId) {
        if (!storeRepository.existsById(storeId)) {
            throw new RuntimeException("Store not found");
        }
        storeRepository.deleteById(storeId);
    }

    // Get a store by ID
    public StoreResponse getStoreById(Long storeId) {
        StoreItem storeItem = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        return mapToResponse(storeItem);
    }

    // Get all stores for a school
    public List<StoreResponse> getAllStoresForSchool(Long schoolId) {
        List<StoreItem> storeItems = storeRepository.findBySchoolId(schoolId);
        return storeItems.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Helper method to map Store to StoreResponse
    private StoreResponse mapToResponse(StoreItem storeItem) {
        StoreResponse response = new StoreResponse();
        response.setId(storeItem.getId());
        response.setName(storeItem.getName());
        response.setDescription(storeItem.getDescription());
        response.setPhoto(storeItem.getPhoto());
        response.setSizes(storeItem.getSizes());
        response.setPrice(storeItem.getPrice());
        return response;
    }
}

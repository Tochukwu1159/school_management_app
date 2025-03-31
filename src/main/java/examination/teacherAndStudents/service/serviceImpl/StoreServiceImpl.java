package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.StoreRequest;
import examination.teacherAndStudents.dto.StoreResponse;
import examination.teacherAndStudents.entity.School;
import examination.teacherAndStudents.entity.Store;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.NotFoundException;
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

    @Override
    public StoreResponse createStore(StoreRequest request) {

        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN)
                .orElseThrow(() -> new NotFoundException("Please login as an Admin"));;

        School school = schoolRepository.findById(admin.getSchool().getId())
                .orElseThrow(() -> new CustomNotFoundException("School not found"));

        Store store = Store.builder()
                .name(request.getName())
                .school(school)
                .build();

        store = storeRepository.save(store);
        return mapToResponse(store);
    }

    @Override
    public StoreResponse editStore(Long storeId, StoreRequest request) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN)
                .orElseThrow(() -> new NotFoundException("Please login as an Admin"));
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomNotFoundException("Store not found"));

        store.setName(request.getName());
        store = storeRepository.save(store);

        return mapToResponse(store);
    }

    @Override
    public void deleteStore(Long storeId) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN)
                .orElseThrow(() -> new NotFoundException("Please login as an Admin"));
        if (!storeRepository.existsById(storeId)) {
            throw new CustomNotFoundException("Store not found");
        }
        storeRepository.deleteById(storeId);
    }

    @Override
    public StoreResponse getStoreById(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomNotFoundException("Store not found"));

        return mapToResponse(store);
    }

    @Override
    public List<StoreResponse> getAllStoresForSchool(Long schoolId) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN)
                .orElseThrow(() -> new NotFoundException("Please login as an Admin"));
        List<Store> stores = storeRepository.findBySchoolId(schoolId);
        return stores.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private StoreResponse mapToResponse(Store store) {
        StoreResponse response = new StoreResponse();
        response.setId(store.getId());
        response.setName(store.getName());
        response.setSchoolId(store.getSchool().getId());
        return response;
    }
}

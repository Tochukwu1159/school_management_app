package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.StoreRequest;
import examination.teacherAndStudents.dto.StoreResponse;

import java.util.List;

public interface StoreService {
    StoreResponse createStore(StoreRequest request);
    StoreResponse editStore(Long storeId, StoreRequest request);
    void deleteStore(Long storeId);
    StoreResponse getStoreById(Long storeId);
    List<StoreResponse> getAllStoresForSchool(Long schoolId);
}

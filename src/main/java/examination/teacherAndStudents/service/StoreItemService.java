package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.StoreItemRequest;
import examination.teacherAndStudents.dto.StoreItemResponse;

import java.util.List;

public interface StoreItemService {
    StoreItemResponse createStoreItem(StoreItemRequest request);
    StoreItemResponse editStoreItem(Long storeId, StoreItemRequest request);
    void deleteStoreItem(Long storeId);
    StoreItemResponse getStoreItemById(Long storeId);
    List<StoreItemResponse> getAllStoreItemsForSchool();
}

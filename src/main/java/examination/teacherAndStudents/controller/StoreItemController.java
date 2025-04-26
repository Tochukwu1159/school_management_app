package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.StoreItemRequest;
import examination.teacherAndStudents.dto.StoreItemResponse;
import examination.teacherAndStudents.service.StoreItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/store-item")
@RequiredArgsConstructor
public class StoreItemController {

    private final StoreItemService storeService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<StoreItemResponse>> createStore(@RequestBody StoreItemRequest request) {
        StoreItemResponse response = storeService.createStoreItem(request);
        ApiResponse<StoreItemResponse> apiResponse = new ApiResponse<>("Store item created successfully", true, response);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PutMapping("/{storeId}")
    public ResponseEntity<ApiResponse<StoreItemResponse>> editStore(@PathVariable Long storeId, @RequestBody StoreItemRequest request) {
        StoreItemResponse response = storeService.editStoreItem(storeId, request);
        ApiResponse<StoreItemResponse> apiResponse = new ApiResponse<>("Store item updated successfully", true, response);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{storeId}")
    public ResponseEntity<ApiResponse<Void>> deleteStore(@PathVariable Long storeId) {
        storeService.deleteStoreItem(storeId);
        ApiResponse<Void> apiResponse = new ApiResponse<>("Store item deleted successfully", true, null);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<ApiResponse<StoreItemResponse>> getStoreById(@PathVariable Long storeId) {
        StoreItemResponse response = storeService.getStoreItemById(storeId);
        ApiResponse<StoreItemResponse> apiResponse = new ApiResponse<>("Store item fetched successfully", true, response);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/school")
    public ResponseEntity<ApiResponse<List<StoreItemResponse>>> getAllStoresForSchool() {
        List<StoreItemResponse> responses = storeService.getAllStoreItemsForSchool();
        ApiResponse<List<StoreItemResponse>> apiResponse = new ApiResponse<>("Store items fetched successfully", true, responses);
        return ResponseEntity.ok(apiResponse);
    }
}

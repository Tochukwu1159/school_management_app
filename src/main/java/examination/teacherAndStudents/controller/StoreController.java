package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.StoreRequest;
import examination.teacherAndStudents.dto.StoreResponse;
import examination.teacherAndStudents.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<StoreResponse>> createStore(@RequestBody StoreRequest request) {
        StoreResponse response = storeService.createStore(request);
        ApiResponse<StoreResponse> apiResponse = new ApiResponse<>("Store created successfully", true, response);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PutMapping("/{storeId}")
    public ResponseEntity<ApiResponse<StoreResponse>> editStore(@PathVariable Long storeId, @RequestBody StoreRequest request) {
        StoreResponse response = storeService.editStore(storeId, request);
        ApiResponse<StoreResponse> apiResponse = new ApiResponse<>("Store updated successfully", true, response);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{storeId}")
    public ResponseEntity<ApiResponse<Void>> deleteStore(@PathVariable Long storeId) {
        storeService.deleteStore(storeId);
        ApiResponse<Void> apiResponse = new ApiResponse<>("Store deleted successfully", true, null);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<ApiResponse<StoreResponse>> getStoreById(@PathVariable Long storeId) {
        StoreResponse response = storeService.getStoreById(storeId);
        ApiResponse<StoreResponse> apiResponse = new ApiResponse<>("Store fetched successfully", true, response);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/school/{schoolId}")
    public ResponseEntity<ApiResponse<List<StoreResponse>>> getAllStoresForSchool(@PathVariable Long schoolId) {
        List<StoreResponse> responses = storeService.getAllStoresForSchool(schoolId);
        ApiResponse<List<StoreResponse>> apiResponse = new ApiResponse<>("Stores fetched successfully", true, responses);
        return ResponseEntity.ok(apiResponse);
    }
}

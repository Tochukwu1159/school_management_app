package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.StoreItemRequest;
import examination.teacherAndStudents.dto.StoreItemResponse;
import examination.teacherAndStudents.entity.Store;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.StoreRepository;
import examination.teacherAndStudents.service.StoreItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/store-item")
public class StoreItemController {

    @Autowired
    private StoreItemService storeService;
    @Autowired
    private StoreRepository storeRepository;

    @PostMapping("/create")
    public ResponseEntity<StoreItemResponse> createStore(
                                                     @RequestBody StoreItemRequest request) {

        StoreItemResponse response = storeService.createStoreItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{storeId}")
    public ResponseEntity<StoreItemResponse> editStore(@PathVariable Long storeId,
                                                       @RequestBody StoreItemRequest request) {
        StoreItemResponse response = storeService.editStoreItem(storeId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{storeId}")
    public ResponseEntity<Void> deleteStore(@PathVariable Long storeId) {
        storeService.deleteStoreItem(storeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<StoreItemResponse> getStoreById(@PathVariable Long storeId) {
        StoreItemResponse response = storeService.getStoreItemById(storeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/school")
    public ResponseEntity<List<StoreItemResponse>> getAllStoresForSchool() {
        List<StoreItemResponse> responses = storeService.getAllStoreItemsForSchool();
        return ResponseEntity.ok(responses);
    }
}


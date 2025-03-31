package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.StoreRequest;
import examination.teacherAndStudents.dto.StoreResponse;
import examination.teacherAndStudents.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stores")
public class StoreController {

    @Autowired
    private StoreService storeService;

    @PostMapping("/create")
    public ResponseEntity<StoreResponse> createStore(@RequestBody StoreRequest request) {
        StoreResponse response = storeService.createStore(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{storeId}")
    public ResponseEntity<StoreResponse> editStore(@PathVariable Long storeId, @RequestBody StoreRequest request) {
        StoreResponse response = storeService.editStore(storeId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{storeId}")
    public ResponseEntity<Void> deleteStore(@PathVariable Long storeId) {
        storeService.deleteStore(storeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<StoreResponse> getStoreById(@PathVariable Long storeId) {
        StoreResponse response = storeService.getStoreById(storeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/school/{schoolId}")
    public ResponseEntity<List<StoreResponse>> getAllStoresForSchool(@PathVariable Long schoolId) {
        List<StoreResponse> responses = storeService.getAllStoresForSchool(schoolId);
        return ResponseEntity.ok(responses);
    }
}

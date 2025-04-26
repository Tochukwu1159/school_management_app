package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.PromotionCriteriaRequest;
import examination.teacherAndStudents.dto.PromotionCriteriaResponse;
import examination.teacherAndStudents.service.PromotionCriteriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/promotion-criteria")
@RequiredArgsConstructor
public class PromotionCriteriaController {

    private final PromotionCriteriaService promotionCriteriaService;

    @PostMapping
    public ResponseEntity<ApiResponse<PromotionCriteriaResponse>> createPromotionCriteria(
            @Valid @RequestBody PromotionCriteriaRequest request) {
        PromotionCriteriaResponse response = promotionCriteriaService.createPromotionCriteria(request);
        ApiResponse<PromotionCriteriaResponse> apiResponse = new ApiResponse<>("Promotion criteria created successfully", true, response);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PromotionCriteriaResponse>> updatePromotionCriteria(
            @PathVariable Long id,
            @Valid @RequestBody PromotionCriteriaRequest request) {
        PromotionCriteriaResponse response = promotionCriteriaService.updatePromotionCriteria(id, request);
        ApiResponse<PromotionCriteriaResponse> apiResponse = new ApiResponse<>("Promotion criteria updated successfully", true, response);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePromotionCriteria(@PathVariable Long id) {
        promotionCriteriaService.deletePromotionCriteria(id);
        ApiResponse<Void> apiResponse = new ApiResponse<>("Promotion criteria deleted successfully", true, null);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();  // No content in the body
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PromotionCriteriaResponse>> getPromotionCriteriaById(@PathVariable Long id) {
        PromotionCriteriaResponse response = promotionCriteriaService.getPromotionCriteriaById(id);
        ApiResponse<PromotionCriteriaResponse> apiResponse = new ApiResponse<>("Promotion criteria fetched successfully", true, response);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PromotionCriteriaResponse>>> getAllPromotionCriteria() {
        List<PromotionCriteriaResponse> response = promotionCriteriaService.getAllPromotionCriteria();
        ApiResponse<List<PromotionCriteriaResponse>> apiResponse = new ApiResponse<>("All promotion criteria fetched successfully", true, response);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/class-block/{classBlockId}")
    public ResponseEntity<ApiResponse<List<PromotionCriteriaResponse>>> getPromotionCriteriaByClassBlock(
            @PathVariable Long classBlockId) {
        List<PromotionCriteriaResponse> response = promotionCriteriaService.getPromotionCriteriaByClassBlock(classBlockId);
        ApiResponse<List<PromotionCriteriaResponse>> apiResponse = new ApiResponse<>("Promotion criteria for class block fetched successfully", true, response);
        return ResponseEntity.ok(apiResponse);
    }
}

package examination.teacherAndStudents.controller;


import examination.teacherAndStudents.dto.PromotionCriteriaRequest;
import examination.teacherAndStudents.dto.PromotionCriteriaResponse;
import examination.teacherAndStudents.service.PromotionCriteriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<PromotionCriteriaResponse> createPromotionCriteria(
            @Valid @RequestBody PromotionCriteriaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(promotionCriteriaService.createPromotionCriteria(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromotionCriteriaResponse> updatePromotionCriteria(
            @PathVariable Long id,
            @Valid @RequestBody PromotionCriteriaRequest request) {
        return ResponseEntity.ok(promotionCriteriaService.updatePromotionCriteria(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePromotionCriteria(@PathVariable Long id) {
        promotionCriteriaService.deletePromotionCriteria(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromotionCriteriaResponse> getPromotionCriteriaById(@PathVariable Long id) {
        return ResponseEntity.ok(promotionCriteriaService.getPromotionCriteriaById(id));
    }

    @GetMapping
    public ResponseEntity<List<PromotionCriteriaResponse>> getAllPromotionCriteria() {
        return ResponseEntity.ok(promotionCriteriaService.getAllPromotionCriteria());
    }

    @GetMapping("/class-block/{classBlockId}")
    public ResponseEntity<List<PromotionCriteriaResponse>> getPromotionCriteriaByClassBlock(
            @PathVariable Long classBlockId) {
        return ResponseEntity.ok(promotionCriteriaService.getPromotionCriteriaByClassBlock(classBlockId));
    }
}

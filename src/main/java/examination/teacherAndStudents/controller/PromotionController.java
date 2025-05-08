package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.service.PromotionService;
import examination.teacherAndStudents.dto.StudentPromotionRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @PostMapping("/promote")
    public ResponseEntity<String> promoteStudents(@Valid @RequestBody StudentPromotionRequest request) {
        promotionService.promoteStudents(request);
        return ResponseEntity.ok("Students promoted successfully.");
    }
}

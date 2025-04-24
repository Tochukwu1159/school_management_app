package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ScratchCardDTO;
import examination.teacherAndStudents.dto.ScratchCardPurchaseRequest;
import examination.teacherAndStudents.dto.ScratchCardValidationRequest;
import examination.teacherAndStudents.dto.ScratchCardValidationResponse;
import examination.teacherAndStudents.service.ScratchCardAssignmentService;
import examination.teacherAndStudents.service.ScratchCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/scratch-cards")
@RequiredArgsConstructor
public class ScratchCardAdminController {
    private final ScratchCardService scratchCardService;
    private final ScratchCardAssignmentService scratchCardAssignmentService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ScratchCardDTO>> getScratchCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) Long termId) {
        return ResponseEntity.ok(scratchCardService.getGeneratedScratchCards(page, size, sessionId, termId));
    }

    @PostMapping("/validate")
    public ResponseEntity<ScratchCardValidationResponse> validateScratchCard(
            @Valid @RequestBody ScratchCardValidationRequest request) {
        ScratchCardValidationResponse result = scratchCardService.validateScratchCard(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/buy")
    public ResponseEntity<ScratchCardDTO> buyScratch(@Valid @RequestBody ScratchCardPurchaseRequest request) throws Exception {
        ScratchCardDTO result = scratchCardAssignmentService.buyScratch(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/student")
    public ResponseEntity<ScratchCardDTO> getStudentScratchCard(
            @RequestParam Long sessionId,
            @RequestParam Long termId) throws Exception {
        ScratchCardDTO result = scratchCardAssignmentService.getStudentScratchCard(sessionId, termId);
        return ResponseEntity.ok(result);
    }
}
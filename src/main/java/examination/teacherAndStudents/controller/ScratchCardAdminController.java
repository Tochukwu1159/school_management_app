package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.service.ScratchCardAssignmentService;
import examination.teacherAndStudents.service.ScratchCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing scratch cards for admin operations.
 */
@RestController
@RequestMapping("/api/v1/scratch-cards")
@RequiredArgsConstructor
public class ScratchCardAdminController {

    private final ScratchCardService scratchCardService;
    private final ScratchCardAssignmentService scratchCardAssignmentService;

    /**
     * Get list of generated scratch cards for the admin.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<ScratchCardDTO>>> getScratchCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) Long termId) {
        Page<ScratchCardDTO> result = scratchCardService.getGeneratedScratchCards(page, size, sessionId, termId);
        return ResponseEntity.ok(new ApiResponse<>("Fetched scratch cards successfully", true, result));
    }

    /**
     * Validate scratch card for a user.
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<ScratchCardValidationResponse>> validateScratchCard(
            @Valid @RequestBody ScratchCardValidationRequest request) {
        ScratchCardValidationResponse result = scratchCardService.validateScratchCard(request);
        return ResponseEntity.ok(new ApiResponse<>("Scratch card validation successful", true, result));
    }

    /**
     * Buy scratch card.
     */
    @PostMapping("/buy")
    public ResponseEntity<ApiResponse<ScratchCardDTO>> buyScratch(@Valid @RequestBody ScratchCardPurchaseRequest request) throws Exception {
        ScratchCardDTO result = scratchCardAssignmentService.buyScratch(request);
        return ResponseEntity.ok(new ApiResponse<>("Scratch card purchased successfully", true, result));
    }

    /**
     * Get student's scratch card for the given session and term.
     */
    @GetMapping("/student")
    public ResponseEntity<ApiResponse<ScratchCardDTO>> getStudentScratchCard(
            @RequestParam Long sessionId,
            @RequestParam Long termId) throws Exception {
        ScratchCardDTO result = scratchCardAssignmentService.getStudentScratchCard(sessionId, termId);
        return ResponseEntity.ok(new ApiResponse<>("Fetched student's scratch card successfully", true, result));
    }
}

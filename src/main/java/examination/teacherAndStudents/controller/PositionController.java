package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.service.PositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/position")
public class PositionController {

    @Autowired
    private PositionService positionService;

    @PostMapping("/update/{sessionId}/{term}/{classLevelId}")
    public ResponseEntity<ApiResponse<String>> updateAllPositionsForAClass(
            @PathVariable Long classLevelId,
            @PathVariable Long sessionId,
            @PathVariable Long term) {
        try {
            positionService.updatePositionsForClass(classLevelId, sessionId, term);
            ApiResponse<String> response = new ApiResponse<>("Positions updated successfully", true, null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error updating positions: " + e.getMessage(), false, null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/generate-result-summary/{studentId}/{classLevelId}/{sessionId}/{term}")
    public ResponseEntity<ApiResponse<String>> generateResultSummaryPdf(
            @PathVariable Long studentId,
            @PathVariable Long classLevelId,
            @PathVariable Long sessionId,
            @PathVariable Long term) {
        try {
            positionService.generateResultSummaryPdf(studentId, classLevelId, sessionId, term);
            ApiResponse<String> response = new ApiResponse<>("Result summary PDF generation initiated successfully.", true, null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error generating result summary PDF: " + e.getMessage(), false, null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


}

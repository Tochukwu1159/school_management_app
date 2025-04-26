package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.AcademicSessionRequest;
import examination.teacherAndStudents.dto.AcademicSessionResponse;
import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.GraduationRequest;
import examination.teacherAndStudents.service.AcademicSessionService;
import examination.teacherAndStudents.utils.SessionPromotion;
import examination.teacherAndStudents.utils.SessionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/academic-sessions")
@RequiredArgsConstructor
public class AcademicSessionController {

    private final AcademicSessionService academicSessionService;

    @PostMapping
    public ResponseEntity<ApiResponse<AcademicSessionResponse>> createAcademicSession(@RequestBody AcademicSessionRequest request) {
        try {
            AcademicSessionResponse response = academicSessionService.createAcademicSession(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("Academic session created successfully", true, response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error creating academic session: " + e.getMessage(), false));
        }
    }

    @PostMapping("/graduate-students")
    public ResponseEntity<ApiResponse<Void>> graduateStudents(@RequestBody GraduationRequest request) {
        try {
            academicSessionService.graduateStudentsForSession(
                    request.getAcademicSessionId(),
                    request.getClassBlockIds()
            );
            return ResponseEntity.ok(new ApiResponse<>("Students graduated successfully", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error graduating students: " + e.getMessage(), false));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AcademicSessionResponse>> updateAcademicSession(@PathVariable Long id, @RequestBody AcademicSessionRequest request) {
        try {
            AcademicSessionResponse response = academicSessionService.updateAcademicSession(id, request);
            return ResponseEntity.ok(new ApiResponse<>("Academic session updated successfully", true, response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error updating academic session: " + e.getMessage(), false));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AcademicSessionResponse>> getAcademicSessionById(@PathVariable Long id) {
        try {
            AcademicSessionResponse response = academicSessionService.getAcademicSessionById(id);
            return ResponseEntity.ok(new ApiResponse<>("Academic session retrieved successfully", true, response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error retrieving academic session: " + e.getMessage(), false));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AcademicSessionResponse>>> getAllAcademicSessions(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) SessionStatus status,
            @RequestParam(required = false) SessionPromotion promotion,
            @RequestParam(required = false) Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "startDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        try {
            Page<AcademicSessionResponse> response = academicSessionService.getAllAcademicSessions(
                    name, status, promotion, id,
                    page, size, sortBy, sortDirection);
            return ResponseEntity.ok(new ApiResponse<>("Academic sessions retrieved successfully", true, response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error retrieving academic sessions: " + e.getMessage(), false));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAcademicSession(@PathVariable Long id) {
        try {
            academicSessionService.deleteAcademicSession(id);
            return ResponseEntity.ok(new ApiResponse<>("Academic session deleted successfully", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error deleting academic session: " + e.getMessage(), false));
        }
    }
}
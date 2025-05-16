package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.StudentManifestRequest;
import examination.teacherAndStudents.dto.StudentManifestResponse;
import examination.teacherAndStudents.service.StudentManifestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/student-manifests")
@RequiredArgsConstructor
public class StudentManifestController {

    private final StudentManifestService manifestService;

    @PostMapping
    public ResponseEntity<ApiResponse<StudentManifestResponse>> createOrUpdateManifest(@RequestBody @Valid StudentManifestRequest request) {
        try {
            StudentManifestResponse response = manifestService.createOrUpdateManifest(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("Manifest entry created/updated successfully", true, response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(e.getMessage(), false));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentManifestResponse>> getManifestById(@PathVariable Long id) {
        try {
            StudentManifestResponse response = manifestService.getManifestById(id);
            return ResponseEntity.ok(new ApiResponse<>("Manifest retrieved successfully", true, response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>( e.getMessage(), false));
        }
    }

    @GetMapping("/route/{routeId}")
    public ResponseEntity<ApiResponse<Page<StudentManifestResponse>>> getManifestsByTripId(
            @PathVariable Long routeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(required = false) Long academicSessionId,
            @RequestParam(required = false) Long studentTermId,
            @RequestParam(required = false) Long profileId,
            @RequestParam(required = false) String status) {
        try {
            Page<StudentManifestResponse> response = manifestService.getManifestsByTripId(
                    routeId, page, size, sortBy, sortDirection, academicSessionId, studentTermId, profileId, status);
            return ResponseEntity.ok(new ApiResponse<>("Manifests retrieved successfully", true, response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(e.getMessage(), false));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteManifest(@PathVariable Long id) {
        try {
            manifestService.deleteManifest(id);
            return ResponseEntity.ok(new ApiResponse<>("Manifest deleted successfully", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>( e.getMessage(), false));
        }
    }
}
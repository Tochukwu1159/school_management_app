package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.SessionNameRequest;
import examination.teacherAndStudents.dto.SessionNameResponse;
import examination.teacherAndStudents.service.SessionNameService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/session-names")
@RequiredArgsConstructor
public class SessionNameController {

    private final SessionNameService sessionNameService;

    @PostMapping
    public ResponseEntity<ApiResponse<SessionNameResponse>> createSessionName(@RequestBody SessionNameRequest request) {
        try {
            SessionNameResponse response = sessionNameService.createSessionName(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("Session name created successfully", true, response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error creating session name: " + e.getMessage(), false));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SessionNameResponse>> updateSessionName(@PathVariable Long id, @RequestBody SessionNameRequest request) {
        try {
            SessionNameResponse response = sessionNameService.updateSessionName(id, request);
            return ResponseEntity.ok(new ApiResponse<>("Session name updated successfully", true, response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error updating session name: " + e.getMessage(), false));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SessionNameResponse>> getSessionNameById(@PathVariable Long id) {
        try {
            SessionNameResponse response = sessionNameService.getSessionNameById(id);
            return ResponseEntity.ok(new ApiResponse<>("Session name retrieved successfully", true, response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error retrieving session name: " + e.getMessage(), false));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<SessionNameResponse>>> getAllSessionNames(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        try {
            Page<SessionNameResponse> response = sessionNameService.getAllSessionNames(page, size, sortBy, sortDirection);
            return ResponseEntity.ok(new ApiResponse<>("Session names retrieved successfully", true, response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error retrieving session names: " + e.getMessage(), false));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSessionName(@PathVariable Long id) {
        try {
            sessionNameService.deleteSessionName(id);
            return ResponseEntity.ok(new ApiResponse<>("Session name deleted successfully", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error deleting session name: " + e.getMessage(), false));
        }
    }
}
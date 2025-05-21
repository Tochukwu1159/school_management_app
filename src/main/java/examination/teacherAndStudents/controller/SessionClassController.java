package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.SessionClassRequest;
import examination.teacherAndStudents.dto.SessionClassResponse;
import examination.teacherAndStudents.service.SessionClassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/session-classes")
@RequiredArgsConstructor
public class SessionClassController {

    private final SessionClassService sessionClassService;

    @PostMapping
    public ResponseEntity<SessionClassResponse> addProfilesToSessionClass(@Valid @RequestBody SessionClassRequest request) {
        SessionClassResponse response = sessionClassService.addProfilesToSessionClass(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionClassResponse> getSessionClassById(@PathVariable Long id) {
        SessionClassResponse response = sessionClassService.getSessionClassById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<SessionClassResponse>> getAllSessionClasses(
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) Long classBlockId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        Page<SessionClassResponse> response = sessionClassService.getAllSessionClasses(
                sessionId, classBlockId, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SessionClassResponse> updateSessionClass(
            @PathVariable Long id, @Valid @RequestBody SessionClassRequest request) {
        SessionClassResponse response = sessionClassService.updateSessionClass(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSessionClass(@PathVariable Long id) {
        sessionClassService.deleteSessionClass(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{sessionClassId}/assignments/{assignmentId}")
    public ResponseEntity<SessionClassResponse> assignAssignmentToSessionClass(
            @PathVariable Long sessionClassId, @PathVariable Long assignmentId) {
        SessionClassResponse response = sessionClassService.assignAssignmentToSessionClass(sessionClassId, assignmentId);
        return ResponseEntity.ok(response);
    }
}
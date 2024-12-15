
        package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.AcademicSessionRequest;
import examination.teacherAndStudents.dto.AcademicSessionResponse;
import examination.teacherAndStudents.service.AcademicSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/academic-sessions")
@RequiredArgsConstructor
public class AcademicSessionController {

    private final AcademicSessionService academicSessionService;

    @PostMapping
    public ResponseEntity<AcademicSessionResponse> createAcademicSession(@RequestBody AcademicSessionRequest request) {
        return new ResponseEntity<>(academicSessionService.createAcademicSession(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AcademicSessionResponse> updateAcademicSession(@PathVariable Long id, @RequestBody AcademicSessionRequest request) {
        return ResponseEntity.ok(academicSessionService.updateAcademicSession(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AcademicSessionResponse> getAcademicSessionById(@PathVariable Long id) {
        return ResponseEntity.ok(academicSessionService.getAcademicSessionById(id));
    }

    @GetMapping
    public ResponseEntity<List<AcademicSessionResponse>> getAllAcademicSessions() {
        return ResponseEntity.ok(academicSessionService.getAllAcademicSessions());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAcademicSession(@PathVariable Long id) {
        academicSessionService.deleteAcademicSession(id);
        return ResponseEntity.noContent().build();
    }
}

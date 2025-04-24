
        package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.AcademicSessionRequest;
import examination.teacherAndStudents.dto.AcademicSessionResponse;
import examination.teacherAndStudents.dto.GraduationRequest;
import examination.teacherAndStudents.service.AcademicSessionService;
import examination.teacherAndStudents.utils.SessionPromotion;
import examination.teacherAndStudents.utils.SessionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

    @PostMapping("/graduate-students")
    public ResponseEntity<Void> graduateStudents(
            @RequestBody GraduationRequest request) {
        academicSessionService.graduateStudentsForSession(
                request.getAcademicSessionId(),
                request.getClassBlockIds()
        );
        return ResponseEntity.ok().build();
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
    public ResponseEntity<Page<AcademicSessionResponse>> getAllAcademicSessions(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) SessionStatus status,
            @RequestParam(required = false) SessionPromotion promotion,
            @RequestParam(required = false) Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "startDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        return ResponseEntity.ok(academicSessionService.getAllAcademicSessions(
                name, status, promotion, id,
                page, size, sortBy, sortDirection));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAcademicSession(@PathVariable Long id) {
        academicSessionService.deleteAcademicSession(id);
        return ResponseEntity.noContent().build();
    }
}

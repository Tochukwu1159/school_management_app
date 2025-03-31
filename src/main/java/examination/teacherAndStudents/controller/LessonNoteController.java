package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.LessonNoteResponse;
import examination.teacherAndStudents.entity.LessonNote;
import examination.teacherAndStudents.service.LessonNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/lesson-notes")
public class LessonNoteController {

    private final LessonNoteService lessonNoteService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadLessonNote(@RequestBody LessonNote lessonNote) {
        LessonNoteResponse savedLessonNote = lessonNoteService.saveLessonNote(lessonNote);
        return ResponseEntity.ok("Lesson note uploaded successfully with ID: " + savedLessonNote.getId());
    }

    @GetMapping("/all")
    public ResponseEntity<Page<LessonNoteResponse>> getAllLessonNotes(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long studentTermId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Page<LessonNoteResponse> lessonNotesPage = lessonNoteService.getAllLessonNotes(
                id,
                title,
                studentTermId,
                teacherId,
                createdAt,
                page,
                size,
                sortBy,
                sortDirection);

        return ResponseEntity.ok(lessonNotesPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LessonNoteResponse> getLessonNoteById(@PathVariable Long id) {
        LessonNoteResponse lessonNote = lessonNoteService.getLessonNoteById(id);
        return ResponseEntity.ok(lessonNote);
    }

    // Other endpoints for scoring, reviewing, etc.
}

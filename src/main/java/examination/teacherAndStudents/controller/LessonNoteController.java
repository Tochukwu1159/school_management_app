package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.entity.LessonNote;
import examination.teacherAndStudents.service.LessonNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/lesson-notes")
public class LessonNoteController {

    private final LessonNoteService lessonNoteService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadLessonNote(@RequestBody LessonNote lessonNote) {
        LessonNote savedLessonNote = lessonNoteService.saveLessonNote(lessonNote);
        return ResponseEntity.ok("Lesson note uploaded successfully with ID: " + savedLessonNote.getId());
    }

    @GetMapping("/all")
    public ResponseEntity<List<LessonNote>> getAllLessonNotes() {
        List<LessonNote> lessonNotes = lessonNoteService.getAllLessonNotes();
        return ResponseEntity.ok(lessonNotes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LessonNote> getLessonNoteById(@PathVariable Long id) {
        LessonNote lessonNote = lessonNoteService.getLessonNoteById(id);
        return ResponseEntity.ok(lessonNote);
    }

    // Other endpoints for scoring, reviewing, etc.
}

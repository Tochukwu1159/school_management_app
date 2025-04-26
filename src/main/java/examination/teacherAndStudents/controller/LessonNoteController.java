package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.LessonNoteResponse;
import examination.teacherAndStudents.entity.LessonNote;
import examination.teacherAndStudents.service.LessonNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/lesson-notes")
public class LessonNoteController {

    private final LessonNoteService lessonNoteService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<LessonNoteResponse>> uploadLessonNote(@RequestBody LessonNote lessonNote) {
        LessonNoteResponse savedLessonNote = lessonNoteService.saveLessonNote(lessonNote);
        ApiResponse<LessonNoteResponse> apiResponse = new ApiResponse<>("Lesson note uploaded successfully", true, savedLessonNote);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Page<LessonNoteResponse>>> getAllLessonNotes(
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

        ApiResponse<Page<LessonNoteResponse>> apiResponse = new ApiResponse<>("Lesson notes fetched successfully", true, lessonNotesPage);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LessonNoteResponse>> getLessonNoteById(@PathVariable Long id) {
        LessonNoteResponse lessonNote = lessonNoteService.getLessonNoteById(id);
        ApiResponse<LessonNoteResponse> apiResponse = new ApiResponse<>("Lesson note fetched successfully", true, lessonNote);
        return ResponseEntity.ok(apiResponse);
    }

    // Other endpoints for scoring, reviewing, etc.
}

package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.LessonNoteResponse;
import examination.teacherAndStudents.entity.LessonNote;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public interface LessonNoteService {
    LessonNoteResponse saveLessonNote(LessonNote lessonNote);
    Page<LessonNoteResponse> getAllLessonNotes(
            Long id,
            String title,
            Long studentTermId,
            Long teacherId,
            LocalDateTime createdAt,
            int page,
            int size,
            String sortBy,
            String sortDirection);
    LessonNoteResponse getLessonNoteById(Long id);
}

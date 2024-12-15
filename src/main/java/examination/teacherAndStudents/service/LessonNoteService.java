package examination.teacherAndStudents.service;

import examination.teacherAndStudents.entity.LessonNote;

import java.util.List;

public interface LessonNoteService {
    LessonNote saveLessonNote(LessonNote lessonNote);
    List<LessonNote> getAllLessonNotes();
    LessonNote getLessonNoteById(Long id);
}

package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.entity.LessonNote;
import examination.teacherAndStudents.error_handler.LessonNoteNotFoundException;
import examination.teacherAndStudents.repository.LessonNoteRepository;
import examination.teacherAndStudents.service.LessonNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@RequiredArgsConstructor
@Service
public class LessonNoteServiceImpl implements LessonNoteService {

        private final LessonNoteRepository lessonNoteRepository;

        public LessonNote saveLessonNote(LessonNote lessonNote) {
            // Add validation or business logic if needed
            return lessonNoteRepository.save(lessonNote);
        }

        public List<LessonNote> getAllLessonNotes() {
            return lessonNoteRepository.findAll();
        }

        public LessonNote getLessonNoteById(Long id) {
            return lessonNoteRepository.findById(id)
                    .orElseThrow(() -> new LessonNoteNotFoundException("Lesson note not found with id: " + id));
        }

        // Other methods for scoring, updating, etc.
    }


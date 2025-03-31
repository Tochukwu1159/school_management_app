package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.LessonNoteResponse;
import examination.teacherAndStudents.entity.LessonNote;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.LessonNoteNotFoundException;
import examination.teacherAndStudents.repository.LessonNoteRepository;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.service.LessonNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@RequiredArgsConstructor
@Service
public class LessonNoteServiceImpl implements LessonNoteService {

    private final LessonNoteRepository lessonNoteRepository;
    private final ProfileRepository profileRepository;

    @Override
    public LessonNoteResponse saveLessonNote(LessonNote lessonNote) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            Profile profile = profileRepository.findByUserEmail(email)
                    .orElseThrow(() -> new CustomNotFoundException("Please login"));

            lessonNote.setTeacher(profile.getUser());
            lessonNote.setSchool(profile.getUser().getSchool());
            LessonNote savedNote = lessonNoteRepository.save(lessonNote);
            return mapToLessonNoteResponse(savedNote);
        } catch (CustomNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomInternalServerException("Error saving lesson note: " + e.getMessage());
        }
    }

    @Override
    public Page<LessonNoteResponse> getAllLessonNotes(
            Long id,
            String title,
            Long studentTermId,
            Long teacherId,
            LocalDateTime createdAt,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            Profile profile = profileRepository.findByUserEmail(email)
                    .orElseThrow(() -> new CustomNotFoundException("Please login"));

            // Create Pageable object
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            // Fetch filtered lesson notes
            Page<LessonNote> lessonNotesPage = lessonNoteRepository.findAllBySchoolWithFilters(
                    profile.getUser().getSchool(),
                    id,
                    title,
                    studentTermId,
                    teacherId,
                    createdAt,
                    pageable);

            // Map to response DTO
            return lessonNotesPage.map(this::mapToLessonNoteResponse);
        } catch (CustomNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomInternalServerException("Error fetching lesson notes: " + e.getMessage());
        }
    }

    private LessonNoteResponse mapToLessonNoteResponse(LessonNote lessonNote) {
        return LessonNoteResponse.builder()
                .id(lessonNote.getId())
                .lessonPlanId(lessonNote.getLessonPlan() != null ? lessonNote.getLessonPlan().getId() : null)
                .topic(lessonNote.getTopic())
                .keyPoints(lessonNote.getKeyPoints())
                .functions(lessonNote.getFunctions())
                .title(lessonNote.getTitle())
                .content(lessonNote.getContent())
                .diagram(lessonNote.getDiagram())
                .studentTermId(lessonNote.getStudentTerm().getId())
                .studentTermName(lessonNote.getStudentTerm().getName())
                .teacherId(lessonNote.getTeacher().getId())
                .teacherName(lessonNote.getTeacher().getFirstName() + " " + lessonNote.getTeacher().getLastName())
                .createdAt(lessonNote.getCreatedAt())
                .updatedAt(lessonNote.getUpdatedAt())
                .build();
    }

    @Override
    public LessonNoteResponse getLessonNoteById(Long id) {
        try {
            LessonNote lessonNote = lessonNoteRepository.findById(id)
                    .orElseThrow(() -> new LessonNoteNotFoundException("Lesson note not found"));
            return mapToLessonNoteResponse(lessonNote);
        } catch (LessonNoteNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomInternalServerException("Error fetching lesson note: " + e.getMessage());
        }

        // Other methods for scoring, updating, etc.
    }
}


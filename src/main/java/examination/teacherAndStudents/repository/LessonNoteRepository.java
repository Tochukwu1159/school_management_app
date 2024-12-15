package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Hostel;
import examination.teacherAndStudents.entity.LessonNote;
import examination.teacherAndStudents.utils.AvailabilityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonNoteRepository extends JpaRepository<LessonNote, Long> {
}
package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Hostel;
import examination.teacherAndStudents.entity.LessonNote;
import examination.teacherAndStudents.entity.School;
import examination.teacherAndStudents.utils.AvailabilityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface LessonNoteRepository extends JpaRepository<LessonNote, Long> {
    @Query("SELECT l FROM LessonNote l WHERE " +
            "l.teacher.school = :school AND " +
            "(:id IS NULL OR l.id = :id) AND " +
            "(:title IS NULL OR LOWER(l.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:studentTermId IS NULL OR l.studentTerm.id = :studentTermId) AND " +
            "(:teacherId IS NULL OR l.teacher.id = :teacherId) AND " +
            "(:createdAt IS NULL OR l.createdAt >= :createdAt)")
    Page<LessonNote> findAllBySchoolWithFilters(
            @Param("school") School school,
            @Param("id") Long id,
            @Param("title") String title,
            @Param("studentTermId") Long studentTermId,
            @Param("teacherId") Long teacherId,
            @Param("createdAt") LocalDateTime createdAt,
            Pageable pageable);}
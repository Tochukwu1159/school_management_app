package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Homework;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface HomeworkRepository extends JpaRepository<Homework, Long> {
    List<Homework> findBySubjectId(Long subjectId);

    @Query("SELECT h FROM Homework h WHERE "
            + "(:subjectId IS NULL OR h.subject.id = :subjectId) AND "
            + "(:classBlockId IS NULL OR h.classBlock.id = :classBlockId) AND "
            + "(:termId IS NULL OR h.term.id = :termId) AND "
            + "(:submissionDate IS NULL OR h.submissionDate = :submissionDate) AND "
            + "(:title IS NULL OR LOWER(h.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND "
            + "h.school.id = :schoolId")
    Page<Homework> findBySubjectIdAndFilters(
            @Param("subjectId") Long subjectId,
            @Param("classBlockId") Long classBlockId,
            @Param("termId") Long termId,
            @Param("submissionDate") LocalDateTime submissionDate,
            @Param("title") String title,
            @Param("schoolId") Long schoolId,
            Pageable pageable
    );;

    @Query("SELECT h FROM Homework h WHERE "
            + "(:subjectId IS NULL OR h.subject.id = :subjectId) AND "
            + "h.classBlock.id = :classBlockId AND "
            + "(:termId IS NULL OR h.term.id = :termId) AND "
            + "(:submissionDate IS NULL OR h.submissionDate = :submissionDate) AND "
            + "(:title IS NULL OR LOWER(h.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND "
            + "h.school.id = :schoolId")
    Page<Homework> findBySubjectIdAndFiltersForStudent(
            @Param("subjectId") Long subjectId,
            @Param("classBlockId") Long classBlockId,
            @Param("termId") Long termId,
            @Param("submissionDate") LocalDateTime submissionDate,
            @Param("title") String title,
            @Param("schoolId") Long schoolId,
            Pageable pageable
    );}

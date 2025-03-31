package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.HomeworkSubmission;
import examination.teacherAndStudents.utils.SubmissionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface HomeworkSubmissionRepository extends JpaRepository<HomeworkSubmission, Long> {
    List<HomeworkSubmission> findByHomeworkId(Long homeworkId);

    @Query("SELECT hs FROM HomeworkSubmission hs WHERE " +
            "(:homeworkId IS NULL OR hs.homework.id = :homeworkId) AND " +
            "(:studentId IS NULL OR hs.student.id = :studentId) AND " +
            "(:submittedAt IS NULL OR hs.submittedAt >= :submittedAt) AND " +
            "(:status IS NULL OR hs.status = :status) AND " +
            "hs.student.user.id = :userId")
    Page<HomeworkSubmission> findByHomeworkIdAndFilters(
            @Param("homeworkId") Long homeworkId,
            @Param("studentId") Long studentId,
            @Param("submittedAt") LocalDateTime submittedAt,
            @Param("status") SubmissionStatus status,
            @Param("userId") Long userId,
            Pageable pageable);}

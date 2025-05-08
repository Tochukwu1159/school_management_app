package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.ExamSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;

public interface ExamScheduleRepository extends JpaRepository<ExamSchedule, Long> {

    @Query("SELECT s FROM ExamSchedule s WHERE " +
            "(:subjectId IS NULL OR s.subject.id = :subjectId) AND " +
            "(:teacherId IS NULL OR s.teacher.id = :teacherId) AND " +
            "(:examDate IS NULL OR s.examDate = :examDate)")
    Page<ExamSchedule> findAllWithFilters(
            @Param("subjectId") Long subjectId,
            @Param("teacherId") Long teacherId,
            @Param("examDate") LocalDate examDate,
            Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN TRUE ELSE FALSE END FROM ExamSchedule s " +
            "WHERE s.classBlock.id = :classBlockId AND s.examDate = :examDate " +
            "AND ((s.startTime < :endTime AND s.endTime > :startTime))")
    boolean existsOverlappingSchedule(
            @Param("classBlockId") Long classBlockId,
            @Param("examDate") LocalDate examDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);
}

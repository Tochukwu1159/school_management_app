package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.SubjectSchedule;
import examination.teacherAndStudents.utils.DayOfWeek;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubjectScheduleRepository extends JpaRepository<SubjectSchedule, Long> {
    @Query("SELECT ss FROM SubjectSchedule ss WHERE ss.timetable.classBlock.id = :classBlockId AND ss.timetable.term.id = :termId AND ss.timetable.dayOfWeek = :dayOfWeek")
    List<SubjectSchedule> findByClassBlockIdAndTermIdAndDayOfWeek(@Param("classBlockId") Long classBlockId, @Param("termId") Long termId, @Param("dayOfWeek") DayOfWeek dayOfWeek);

    @Query("SELECT s FROM SubjectSchedule s WHERE s.teacher = :teacher AND s.timetable.school.id = :schoolId AND s.timetable.dayOfWeek = :dayOfWeek")
    List<SubjectSchedule> findByTeacherAndSchoolIdAndTimetableDayOfWeek(@Param("teacher") Profile teacher, @Param("schoolId") Long schoolId, @Param("dayOfWeek") DayOfWeek dayOfWeek);

    @Query("SELECT s FROM SubjectSchedule s WHERE s.timetable.id = :timetableId AND s.timetable.dayOfWeek = :dayOfWeek")
    List<SubjectSchedule> findByTimetableIdAndTimetableDayOfWeek(@Param("timetableId") Long timetableId, @Param("dayOfWeek") DayOfWeek dayOfWeek);

    Page<SubjectSchedule> findAllByTimetableSchoolId(Long schoolId, Pageable pageable);
}
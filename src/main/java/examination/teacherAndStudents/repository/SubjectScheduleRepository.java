package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.SubjectSchedule;
import examination.teacherAndStudents.entity.Timetable;
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

    SubjectSchedule findByIdAndTimetableDayOfWeek(Long scheduleId, DayOfWeek dayOfWeek);

    Page<SubjectSchedule> findAllByTimetableSchoolId(Long schoolId, Pageable pageable);

    @Query("SELECT s FROM SubjectSchedule s WHERE s.teacher = :teacher AND s.timetable.school.id = :schoolId")
    List<SubjectSchedule> findByTeacherAndSchoolId(@Param("teacher") Profile teacher, @Param("schoolId") Long schoolId);
}
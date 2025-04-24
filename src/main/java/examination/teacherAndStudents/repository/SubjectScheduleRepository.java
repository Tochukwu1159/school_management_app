package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.ClassLevel;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.SubjectSchedule;
import examination.teacherAndStudents.entity.Timetable;
import examination.teacherAndStudents.utils.DayOfWeek;
import examination.teacherAndStudents.utils.TeachingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
@Repository
public interface SubjectScheduleRepository extends JpaRepository<SubjectSchedule, Long> {

    SubjectSchedule findByIdAndTimetableDayOfWeek(Long scheduleId, DayOfWeek dayOfWeek);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
            "FROM SubjectSchedule s " +
            "WHERE s.timetable = :timetable " +
            "AND s.teacher = :teacherProfile " +
            "AND ((s.startTime < :endTime AND s.endTime > :startTime))")
    boolean existsByTimetableAndTeacherAndTimeOverlap(
            @Param("timetable") Timetable timetable,
            @Param("teacherProfile") Profile teacherProfile,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
//    existsByTimetableAndTeacherAndStartTimeLessThanAndEndTimeGreaterThan
    Page<SubjectSchedule> findAllByTimetableSchoolId(Long id, Pageable pageable);
}

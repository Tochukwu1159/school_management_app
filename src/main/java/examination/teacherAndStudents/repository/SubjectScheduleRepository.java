package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.ClassLevel;
import examination.teacherAndStudents.entity.SubjectSchedule;
import examination.teacherAndStudents.utils.DayOfWeek;
import examination.teacherAndStudents.utils.TeachingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface SubjectScheduleRepository extends JpaRepository<SubjectSchedule, Long> {

    SubjectSchedule findByIdAndTimetableDayOfWeek(Long scheduleId, DayOfWeek dayOfWeek);
}

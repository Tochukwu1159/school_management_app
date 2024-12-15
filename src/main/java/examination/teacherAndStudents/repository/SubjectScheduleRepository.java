package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.ClassLevel;
import examination.teacherAndStudents.entity.SubjectSchedule;
import examination.teacherAndStudents.utils.DayOfWeek;
import examination.teacherAndStudents.utils.TeachingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectScheduleRepository extends JpaRepository<SubjectSchedule, Long> {

    SubjectSchedule findByIdAndTimetableDayOfWeek(Long scheduleId, DayOfWeek dayOfWeek);

    List<SubjectSchedule> findAllByTeachingStatus(TeachingStatus notTaught);
}

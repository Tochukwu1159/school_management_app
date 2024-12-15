package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.SubjectSchedule;
import examination.teacherAndStudents.entity.Timetable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimetableRepository extends JpaRepository<Timetable, Long> {
    Timetable findBySubjectSchedules(SubjectSchedule subjectSchedule);
}

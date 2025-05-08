package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.SubjectSchedule;
import examination.teacherAndStudents.entity.Timetable;
import examination.teacherAndStudents.utils.DayOfWeek;
import examination.teacherAndStudents.utils.TimetableType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TimetableRepository extends JpaRepository<Timetable, Long> {
    Optional<Timetable> findByIdAndSchoolId(Long timetableId, Long id);

    Page<Timetable> findAllBySchoolId(Long id, Pageable pageable);

    boolean existsByClassBlockIdAndTermIdAndDayOfWeek(Long classBlockId, Long termId, DayOfWeek dayOfWeek);}


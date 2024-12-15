package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.SickLeave;
import examination.teacherAndStudents.utils.SickLeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SickLeaveRepository extends JpaRepository<SickLeave, Long> {
    List<SickLeave> findByStatus(SickLeaveStatus pending);
}

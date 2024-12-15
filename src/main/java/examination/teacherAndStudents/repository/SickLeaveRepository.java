package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.SickLeave;
import examination.teacherAndStudents.utils.SickLeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface SickLeaveRepository extends JpaRepository<SickLeave, Long> {
    List<SickLeave> findByStatus(SickLeaveStatus pending);
}

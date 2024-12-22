package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Leave;
import examination.teacherAndStudents.utils.SickLeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SickLeaveRepository extends JpaRepository<Leave, Long> {
    List<Leave> findByStatus(SickLeaveStatus pending);

    Optional<Leave> findByIdAndCancelledIsTrue(Long id);
}

package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.ClassSubject;
import examination.teacherAndStudents.entity.DuePayment;
import examination.teacherAndStudents.utils.StudentTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
@Repository
public interface DuePaymentRepository extends JpaRepository<DuePayment, Long> {
    Collection<DuePayment> findByUserId(Long userId);

    DuePayment findByDueIdAndUserId(Long id, Long userId);
}

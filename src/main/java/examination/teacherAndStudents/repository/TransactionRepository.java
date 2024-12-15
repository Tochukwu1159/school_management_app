package examination.teacherAndStudents.repository;
import examination.teacherAndStudents.entity.Transaction;
import examination.teacherAndStudents.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findTransactionByUserOrderByCreatedAtDesc(Pageable pageable, Optional<User> student);
}

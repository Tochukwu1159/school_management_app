package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;


public interface OrderRepository extends JpaRepository<Order, Long> {
}
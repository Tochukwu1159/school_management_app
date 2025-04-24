package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;


public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
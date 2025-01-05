package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssignedById(Long assignedById);  // Get all tasks assigned by a specific user
    List<Task> findByAssignedToId(Long assignedToId);  // Get all tasks assigned to a specific user
    List<Task> findByStatus(String status);            // Get tasks by status (e.g., "done", "pending")
}

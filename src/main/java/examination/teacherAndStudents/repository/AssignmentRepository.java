package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByTeacherId(Long teacherId);  // Get all assignments by teacher
    List<Assignment> findBySubjectId(Long subjectId);  // Get all assignments by subject
}

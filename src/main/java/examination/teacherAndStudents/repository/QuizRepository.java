package examination.teacherAndStudents.repository;


import examination.teacherAndStudents.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
}
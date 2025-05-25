package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    QuizAttempt findByQuizIdAndStudentId(Long quizId, Long studentId);
}
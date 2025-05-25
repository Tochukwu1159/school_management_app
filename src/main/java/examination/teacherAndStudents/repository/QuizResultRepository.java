package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {
    List<QuizResult> findByQuizId(Long quizId);

    QuizResult findByQuizIdAndStudentId(Long quizId, Long id);
}
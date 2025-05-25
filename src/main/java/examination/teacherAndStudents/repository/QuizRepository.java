package examination.teacherAndStudents.repository;


import examination.teacherAndStudents.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    Optional<Quiz> findByIdAndSchoolId(Long quizId, Long id);

    Optional<Quiz> findByIdAndSchoolIdAndSubjectId(Long quizId, Long id, Long subjectId);
}
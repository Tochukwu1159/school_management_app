package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.utils.StudentTerm;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScoreRepository extends JpaRepository<Score, Long> {


    List<Score> findScoreByUserProfile(Profile student);

    List<Score> findAllByUserProfileAndClassBlockAndTerm(Profile user, ClassBlock userClass, StudentTerm term);

    Score findByUserProfileAndClassBlockIdAndSubjectNameAndAcademicYearAndTerm(Profile userProfile, Long classBlock_id, @NotBlank String subjectName, AcademicSession academicYear, StudentTerm term);

    List<Score> findAllByUserProfileAndClassBlockAndAcademicYearAndTerm(Profile userProfile, ClassBlock userClass, AcademicSession session, StudentTerm term);


    // Additional methods if needed
}

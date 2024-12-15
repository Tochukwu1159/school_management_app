package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.utils.StudentTerm;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {


    List<Score> findScoreByUserProfile(Profile student);

    Score findByUserProfileAndClassBlockIdAndSubjectNameAndAcademicYearAndTerm(Profile userProfile, Long classBlock_id, @NotBlank String subjectName, AcademicSession academicYear, StudentTerm term);

    List<Score> findAllByUserProfileAndClassBlockAndAcademicYearAndTerm(Profile userProfile, ClassBlock userClass, AcademicSession session, StudentTerm term);


    // Additional methods if needed
}

package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.*;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {


    List<Score> findScoreByUserProfile(Profile student);

    Score findByUserProfileAndClassBlockIdAndSubjectNameAndAcademicYearAndStudentTerm(Profile userProfile, Long classBlock_id, @NotBlank String subjectName, AcademicSession academicYear, StudentTerm term);

    List<Score> findAllByUserProfileAndClassBlockAndAcademicYearAndStudentTerm(Profile userProfile, ClassBlock userClass, AcademicSession session, StudentTerm term);


    // Additional methods if needed
}

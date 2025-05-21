package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.*;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {

    @Query("SELECT s FROM Score s WHERE s.userProfile = :userProfile AND s.sessionClass = :sessionClass AND s.academicYear = :academicYear AND s.studentTerm = :studentTerm")
    List<Score> findAllByUserProfileAndSessionClassAndAcademicYearAndStudentTerm(
            @Param("userProfile") Profile userProfile,
            @Param("sessionClass") SessionClass sessionClass,
            @Param("academicYear") AcademicSession academicYear,
            @Param("studentTerm") StudentTerm studentTerm
    );

    @Query("SELECT s FROM Score s WHERE s.userProfile = :userProfile")
    List<Score> findScoreByUserProfile(@Param("userProfile") Profile userProfile);

    @Query("SELECT s FROM Score s WHERE s.userProfile = :userProfile AND s.sessionClass.id = :sessionClassId AND s.subjectName = :subjectName AND s.academicYear = :academicYear AND s.studentTerm = :studentTerm")
    Optional<Score> findByUserProfileAndSessionClassIdAndSubjectNameAndAcademicYearAndStudentTerm(
            @Param("userProfile") Profile userProfile,
            @Param("sessionClassId") Long sessionClassId,
            @Param("subjectName") @NotBlank String subjectName,
            @Param("academicYear") AcademicSession academicYear,
            @Param("studentTerm") StudentTerm studentTerm
    );
}
package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {

    @Query("SELECT p FROM Position p WHERE p.userProfile = :userProfile AND p.sessionClass = :sessionClass AND p.academicYear = :academicYear AND p.studentTerm = :studentTerm")
    Optional<Position> findByUserProfileAndSessionClassAndAcademicYearAndStudentTerm(
            @Param("userProfile") Profile userProfile,
            @Param("sessionClass") SessionClass sessionClass,
            @Param("academicYear") AcademicSession academicYear,
            @Param("studentTerm") StudentTerm studentTerm
    );

    @Query("SELECT p FROM Position p WHERE p.sessionClass = :sessionClass AND p.academicYear = :academicYear AND p.studentTerm = :studentTerm")
    List<Position> findAllBySessionClassAndAcademicYearAndStudentTerm(
            @Param("sessionClass") SessionClass sessionClass,
            @Param("academicYear") AcademicSession academicYear,
            @Param("studentTerm") StudentTerm studentTerm
    );

    @Query("SELECT p FROM Position p WHERE p.sessionClass = :sessionClass AND p.academicYear = :academicYear AND p.studentTerm = :studentTerm AND p.userProfile IN :userProfiles")
    List<Position> findBySessionClassAndAcademicYearAndStudentTermAndUserProfileIn(
            @Param("sessionClass") SessionClass sessionClass,
            @Param("academicYear") AcademicSession academicYear,
            @Param("studentTerm") StudentTerm studentTerm,
            @Param("userProfiles") Collection<Profile> userProfiles
    );
}
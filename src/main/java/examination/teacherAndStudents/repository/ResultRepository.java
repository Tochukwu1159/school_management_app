package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {

    @Query("SELECT r FROM Result r WHERE r.userProfile = :userProfile AND r.sessionClass = :sessionClass AND r.academicYear = :academicYear AND r.studentTerm = :studentTerm")
    List<Result> findAllByUserProfileAndSessionClassAndAcademicYearAndStudentTerm(
            @Param("userProfile") Profile userProfile,
            @Param("sessionClass") SessionClass sessionClass,
            @Param("academicYear") AcademicSession academicYear,
            @Param("studentTerm") StudentTerm studentTerm
    );

    @Query("SELECT r FROM Result r WHERE r.userProfile = :userProfile AND r.sessionClass.id = :sessionClassId AND r.id = :subjectId AND r.academicYear = :academicYear AND r.studentTerm = :studentTerm")
    Result findByUserProfileAndSessionClassIdAndSubjectIdAndAcademicYearAndStudentTerm(
            @Param("userProfile") Profile userProfile,
            @Param("sessionClassId") Long sessionClassId,
            @Param("subjectId") Long subjectId,
            @Param("academicYear") AcademicSession academicYear,
            @Param("studentTerm") StudentTerm studentTerm
    );

    @Query("SELECT r FROM Result r WHERE r.sessionClass = :sessionClass AND r.academicYear = :academicYear AND r.studentTerm = :studentTerm")
    List<Result> findAllBySessionClassAndAcademicYearAndStudentTerm(
            @Param("sessionClass") SessionClass sessionClass,
            @Param("academicYear") AcademicSession academicYear,
            @Param("studentTerm") StudentTerm studentTerm
    );
}
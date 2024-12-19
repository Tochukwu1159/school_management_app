package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {

    List<Result> findAllByUserProfileAndClassBlockAndAcademicYearAndStudentTerm(Profile userProfile, ClassBlock userClass, AcademicSession academicYear,StudentTerm term);

    Result findByUserProfileAndClassBlockIdAndSubjectNameAndAcademicYearAndStudentTerm(Profile userProfile, Long classBlock_id, String subjectName, AcademicSession academicYear, StudentTerm term);

    List<Result> findAllByClassBlockAndAcademicYearAndStudentTerm(ClassBlock classBlock, AcademicSession academicYear, StudentTerm studentTerm);
}
package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.utils.StudentTerm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResultRepository extends JpaRepository<Result, Long> {

    List<Result> findAllByUserProfileAndClassBlockAndAcademicYearAndTerm(Profile userProfile, ClassBlock userClass, AcademicSession academicYear,StudentTerm term);

    Result findByUserProfileAndClassBlockIdAndSubjectNameAndAcademicYearAndTerm(Profile userProfile, Long classBlock_id, String subjectName, AcademicSession academicYear, StudentTerm term);
}
package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.utils.StudentTerm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PositionRepository extends JpaRepository<Position, Long> {
    Position findByUserProfileAndClassBlockAndAcademicYearAndTerm(Profile user, ClassBlock userClass,AcademicSession academicSession, StudentTerm term);

    List<Position> findAllByClassBlockAndAcademicYearAndTerm(ClassBlock studentClass, AcademicSession academicSession,  StudentTerm term);
}

package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {
    Position findByUserProfileAndClassBlockAndAcademicYearAndStudentTerm(Profile user, ClassBlock userClass,AcademicSession academicSession, StudentTerm term);

    List<Position> findAllByClassBlockAndAcademicYearAndStudentTerm(ClassBlock studentClass, AcademicSession academicSession,  StudentTerm term);

    Collection<Position> findByClassBlockAndAcademicYearAndStudentTerm(ClassBlock classBlock, AcademicSession academicYear, StudentTerm studentTerm);

        Collection<Position> findByClassBlockAndAcademicYearAndStudentTermAndUserProfileIn(ClassBlock classBlock, AcademicSession academicYear, StudentTerm studentTerm, Collection<Profile> userProfile);
}

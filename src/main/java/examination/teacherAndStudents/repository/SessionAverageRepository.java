package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.SessionAverage;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.AcademicSession;
import examination.teacherAndStudents.entity.ClassBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionAverageRepository extends JpaRepository<SessionAverage, Long> {
    SessionAverage findByUserProfileAndAcademicYearAndClassBlock(Profile userProfile, AcademicSession academicYear, ClassBlock classBlock);

    List<SessionAverage> findAllByClassBlockAndAcademicYear(ClassBlock presentClass, AcademicSession currentSession);
}

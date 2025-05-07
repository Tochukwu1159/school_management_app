package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.SessionAverage;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.AcademicSession;
import examination.teacherAndStudents.entity.ClassBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionAverageRepository extends JpaRepository<SessionAverage, Long> {
    SessionAverage findByUserProfileAndAcademicYearAndClassBlock(Profile userProfile, AcademicSession academicYear, ClassBlock classBlock);

    List<SessionAverage> findAllByClassBlockAndAcademicYear(ClassBlock presentClass, AcademicSession currentSession);

    List<SessionAverage> findTop5ByClassBlockAndAcademicYearOrderByAverageScoreDesc(ClassBlock classBlock, AcademicSession academicYear);

    @Query("SELECT sa FROM SessionAverage sa WHERE sa.academicYear = :academicYear " +
            "ORDER BY sa.classBlock.id, sa.averageScore DESC")
    List<SessionAverage> findTop5ByAcademicYear(@Param("academicYear") AcademicSession academicYear);

    List<SessionAverage> findAllByAcademicYearAndClassBlock(AcademicSession academicSession, ClassBlock classBlock);
}

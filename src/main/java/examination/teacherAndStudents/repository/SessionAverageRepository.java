package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionAverageRepository extends JpaRepository<SessionAverage, Long> {
    SessionAverage findByUserProfileAndAcademicYearAndSessionClass(Profile userProfile, AcademicSession academicYear, SessionClass sessionClass);

    List<SessionAverage> findAllBySessionClassAndAcademicYear(SessionClass presentClass, AcademicSession currentSession);

    List<SessionAverage> findTop5BySessionClassAndAcademicYearOrderByAverageScoreDesc(SessionClass SessionClass, AcademicSession academicYear);

//    @Query("SELECT sa FROM SessionAverage sa WHERE sa.academicYear = :academicYear " +
//            "ORDER BY sa.sessionClass.id, sa.averageScore DESC")
//    List<SessionAverage> findTop5ByAcademicYear(@Param("academicYear") AcademicSession academicYear);

    List<SessionAverage> findAllByAcademicYearAndSessionClass(AcademicSession academicSession, SessionClass SessionClass);

    @Query("SELECT sa FROM SessionAverage sa WHERE sa.academicYear.id = :academicYearId " +
            "ORDER BY sa.sessionClass.id, sa.averageScore DESC")
    List<SessionAverage> findTop5ByAcademicYearId(Long academicYearId);
}

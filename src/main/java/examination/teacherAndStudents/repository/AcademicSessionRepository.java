package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.AcademicSession;
import examination.teacherAndStudents.utils.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AcademicSessionRepository extends JpaRepository<AcademicSession, Long> {
    List<AcademicSession> findByEndDateBeforeAndStatus(LocalDate now,
                                                       SessionStatus status);

    @Query("SELECT s FROM AcademicSession s WHERE s.resultReadyDate <= :currentDate AND s.status = :status")
    AcademicSession findByResultReadyDateBeforeOrEqualAndStatus(
            @Param("currentDate") LocalDate currentDate,
            @Param("status") SessionStatus status
    );

    AcademicSession findByResultReadyDateBeforeAndStatus(LocalDate now,
                                                         SessionStatus status);

    Optional<AcademicSession> findByResultReadyDateAndStatus(LocalDate today, SessionStatus sessionStatus);
}
package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.AcademicSession;
import examination.teacherAndStudents.utils.SessionPromotion;
import examination.teacherAndStudents.utils.SessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AcademicSessionRepository extends JpaRepository<AcademicSession, Long> {


    Optional<AcademicSession> findByResultReadyDateAndStatus(LocalDate today, SessionStatus sessionStatus);

    @Query("SELECT a FROM AcademicSession a WHERE " +
            "(:schoolId IS NULL OR a.school.id = :schoolId) AND " +
            "(:name IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:status IS NULL OR a.status = :status) AND " +
            "(:promotion IS NULL OR a.sessionPromotion = :promotion) AND " +
            "(:id IS NULL OR a.id = :id)")
    Page<AcademicSession> findAllWithFilters(
            @Param("schoolId") Long schoolId,
            @Param("name") String name,
            @Param("status") SessionStatus status,
            @Param("promotion") SessionPromotion promotion,
            @Param("id") Long id,
            Pageable pageable);

    @Query("SELECT s FROM AcademicSession s " +
            "WHERE s.school.id = :schoolId " +
            "AND s.status = 'ACTIVE' " +
            "AND CURRENT_DATE BETWEEN s.startDate AND s.endDate")
    Optional<AcademicSession> findCurrentSession(@Param("schoolId") Long schoolId);

    @Query("SELECT s FROM AcademicSession s WHERE s.status = 'ACTIVE' " +
            "OR (s.startDate <= :currentDate AND s.endDate >= :currentDate)")
    Optional<AcademicSession> findCurrentSession1(LocalDate currentDate);

//    AcademicSession findByResultReadyDateBeforeOrEqualAndStatus(LocalDate now, SessionStatus sessionStatus);
}
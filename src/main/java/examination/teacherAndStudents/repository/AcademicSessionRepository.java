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
import java.util.Optional;

@Repository
public interface AcademicSessionRepository extends JpaRepository<AcademicSession, Long> {



    @Query("SELECT a FROM AcademicSession a WHERE " +
            "(:schoolId IS NULL OR a.school.id = :schoolId) AND " +
            "(:name IS NULL OR LOWER(a.sessionName.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
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
                "JOIN s.studentTerms t " +
                "WHERE s.school.id = :schoolId " +
                "AND s.status = 'ACTIVE' " +
                "AND CURRENT_DATE BETWEEN t.startDate AND t.endDate")
        Optional<AcademicSession> findCurrentSession(@Param("schoolId") Long schoolId);

    Optional<AcademicSession> findByStatusAndSchoolId(SessionStatus sessionStatus, Long id);
}
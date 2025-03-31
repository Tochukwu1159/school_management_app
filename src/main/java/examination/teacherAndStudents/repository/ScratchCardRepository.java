package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.School;
import examination.teacherAndStudents.entity.SchoolExpense;
import examination.teacherAndStudents.entity.ScratchCard;
import org.apache.commons.lang3.stream.Streams;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScratchCardRepository extends JpaRepository<ScratchCard, Long> {
    Optional<ScratchCard> findByCardNumber(String cardNumber);

    Page<ScratchCard> findBySchoolId(Long id, Pageable pageable);

    @Query("SELECT sc FROM ScratchCard sc " +
            "WHERE sc.student IS NULL " +
            "AND sc.isActive = true " +
            "AND sc.school.id = :schoolId " +
            "AND sc.academicSession.id = :sessionId " +
            "AND sc.studentTerm.id = :termId " +
            "ORDER BY sc.createdAt ASC")
    List<ScratchCard> findAvailableBySchoolSessionAndTerm(
            @Param("schoolId") Long schoolId,
            @Param("sessionId") Long sessionId,
            @Param("termId") Long termId,
            Pageable pageable);

    default Optional<ScratchCard> findFirstAvailableBySchoolSessionAndTerm(
            Long schoolId, Long sessionId, Long termId) {
        Pageable limitOne = PageRequest.of(0, 1);
        List<ScratchCard> results = findAvailableBySchoolSessionAndTerm(
                schoolId, sessionId, termId, limitOne);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Query("SELECT sc FROM ScratchCard sc " +
            "WHERE sc.school.id = :schoolId " +
            "AND (:sessionId IS NULL OR sc.academicSession.id = :sessionId) " +
            "AND (:termId IS NULL OR sc.studentTerm.id = :termId)")
    Page<ScratchCard> findBySchoolAndOptionalSessionAndTerm(
            @Param("schoolId") Long schoolId,
            @Param("sessionId") Long sessionId,
            @Param("termId") Long termId,
            Pageable pageable);

    Optional<ScratchCard> findBySchoolIdAndAcademicSessionIdAndStudentTermIdAndStudentId(Long schoolId, Long sessionId, Long termId, Long studentProfileId);
}

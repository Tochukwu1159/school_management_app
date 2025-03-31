
package examination.teacherAndStudents.repository;


import examination.teacherAndStudents.entity.*;
        import examination.teacherAndStudents.utils.Roles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

@Repository
public interface DuesPaymentRepository extends JpaRepository<DuePayment, Long> {
    DuePayment findByDueIdAndAcademicYearAndStudentTermAndProfile(Long dueId, AcademicSession academicSession, StudentTerm term, Profile profile);
    DuePayment findByDueIdAndAcademicYearAndProfile(Long dueId, AcademicSession academicSession, Profile profile);


    boolean existsByDueAndProfileAndAcademicYearAndStudentTerm(Dues due, Profile profile, AcademicSession session, StudentTerm term);

    boolean existsByDueIdAndAcademicYearAndProfile(Long id, AcademicSession session, Profile profile);

    @Query("SELECT dp FROM DuePayment dp WHERE " +
            "dp.due.school.id = :schoolId AND " +
            "(:id IS NULL OR dp.id = :id) AND " +
            "(:studentTermId IS NULL OR dp.due.studentTerm.id = :studentTermId) AND " +
            "(:academicYearId IS NULL OR dp.due.academicYear.id = :academicYearId) AND " +
            "(:profileId IS NULL OR dp.profile.id = :profileId) AND " +
            "(:dueId IS NULL OR dp.due.id = :dueId) AND " +
            "(:startDate IS NULL OR dp.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR dp.createdAt <= :endDate)")
    Page<DuePayment> findAllBySchoolWithFilters(
            @Param("schoolId") Long schoolId,
            @Param("id") Long id,
            @Param("studentTermId") Long studentTermId,
            @Param("academicYearId") Long academicYearId,
            @Param("profileId") Long profileId,
            @Param("dueId") Long dueId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT dp FROM DuePayment dp WHERE " +
            "dp.profile.id = :userId AND " +
            "(:dueId IS NULL OR dp.due.id = :dueId) AND " +
            "(:studentTermId IS NULL OR dp.studentTerm.id = :studentTermId) AND " +
            "(:academicYearId IS NULL OR dp.academicYear.id = :academicYearId) AND " +
            "(:createdAt IS NULL OR dp.createdAt >= :createdAt)")
    Page<DuePayment> findAllByUserWithFilters(
            @Param("userId") Long userId,
            @Param("dueId") Long dueId,
            @Param("studentTermId") Long studentTermId,
            @Param("academicYearId") Long academicYearId,
            @Param("createdAt") LocalDateTime createdAt,
            Pageable pageable);

    Optional<DuePayment> findByDueAndSchoolIdAndProfileAndAcademicYear(Dues dues, Long id, Profile studentProfile, AcademicSession academicSession);
    // You can add custom query methods if needed
}
package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;


public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByProfile(Profile student);

    List<Payment> findByProfileAndStudentFee(Profile student, Fee fee);

    @Query("SELECT p FROM Payment p WHERE p.studentFee.id = :feeId " +
            "AND p.academicSession = :academicSession " +
            "AND p.profile = :profile")
    Payment findPaymentsForSession(
            @Param("feeId") Long feeId,
            @Param("academicSession") AcademicSession academicSession,
            @Param("profile") Profile profile);


    @Query("SELECT p FROM Payment p WHERE p.studentFee.id = :feeId " +
            "AND p.academicSession = :academicSession " +
            "AND p.profile = :profile " +
            "AND (p.studentTerm IS NULL OR p.studentTerm = :term)")
    Payment findPaymentsForSessionAndTerm(
            @Param("feeId") Long feeId,
            @Param("academicSession") AcademicSession academicSession,
            @Param("profile") Profile profile,
            @Param("term") StudentTerm term);

    boolean existsByStudentFeeAndProfileAndAcademicSessionAndStudentTerm(Fee fee, Profile profile, AcademicSession academicSession, StudentTerm term);

    Optional<Payment> findByStudentFeeAndProfileAndAcademicSessionAndStudentTerm(Fee fee, Profile profile, AcademicSession academicSession, StudentTerm term);

    Optional<Payment> findByStudentFeeAndProfileAndAcademicSession(Fee fee, Profile profile, AcademicSession academicSession);
}
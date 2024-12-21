
package examination.teacherAndStudents.repository;


import examination.teacherAndStudents.entity.*;
        import examination.teacherAndStudents.utils.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface DuesPaymentRepository extends JpaRepository<DuePayment, Long> {
    DuePayment findByDueIdAndAcademicYearAndStudentTermAndProfile(Long dueId, AcademicSession academicSession, StudentTerm term, Profile profile);
    Collection<DuePayment> findByProfileId(Long userId);

    DuePayment findByDueIdAndAcademicYearAndProfile(Long dueId, AcademicSession academicSession, Profile profile);

    DuePayment findByDueAndProfileAndAcademicYearAndStudentTerm(Dues dues, Profile user, AcademicSession academicSession, StudentTerm termId);

    // You can add custom query methods if needed
}
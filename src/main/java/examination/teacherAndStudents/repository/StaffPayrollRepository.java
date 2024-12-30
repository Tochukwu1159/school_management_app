package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.StaffPayroll;
import examination.teacherAndStudents.utils.PaymentStatus;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffPayrollRepository extends JpaRepository<StaffPayroll, Long> {
    List<StaffPayroll> findBySchoolId(Long schoolId);

    Optional<StaffPayroll> findByStaffId(Long staffId);

    @Query("SELECT p FROM StaffPayroll p WHERE p.school.id = :schoolId AND YEAR(p.createdAt) = :year AND MONTH(p.createdAt) = :month AND p.paymentStatus = :paymentStatus")
    List<StaffPayroll> findBySchoolIdAndYearAndMonthAndStatus(Long schoolId, int year, int month, PaymentStatus paymentStatus);

    @Query("SELECT p FROM StaffPayroll p WHERE p.school.id = :schoolId AND YEAR(p.createdAt) = :year AND MONTH(p.createdAt) = :month")
    List<StaffPayroll> findBySchoolIdAndYearAndMonth(Long schoolId, int month, int year);

    Optional<StaffPayroll> findByStaffIdAndSchoolId(Long id, Long id1);
}
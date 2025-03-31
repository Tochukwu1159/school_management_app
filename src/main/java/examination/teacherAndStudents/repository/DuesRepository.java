package examination.teacherAndStudents.repository;


import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.utils.Roles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DuesRepository extends JpaRepository<Dues, Long> {

    @Query("SELECT d FROM Dues d WHERE " +
            "d.school.id = :schoolId AND " +
            "(:id IS NULL OR d.id = :id) AND " +
            "(:studentTermId IS NULL OR d.studentTerm.id = :studentTermId) AND " +
            "(:academicYearId IS NULL OR d.academicYear.id = :academicYearId)")
    Page<Dues> findAllBySchoolWithFilters(
            @Param("schoolId") Long schoolId,
            @Param("id") Long id,
            @Param("studentTermId") Long studentTermId,
            @Param("academicYearId") Long academicYearId,
            Pageable pageable);    // You can add custom query methods if needed

    boolean existsByPurposeAndStudentTermAndAcademicYear(String purpose, StudentTerm studentTerm, AcademicSession academicSession);

    boolean existsByPurposeAndStudentTermAndAcademicYearAndIdNot(String purpose, StudentTerm studentTerm, AcademicSession academicSession, Long id);
}
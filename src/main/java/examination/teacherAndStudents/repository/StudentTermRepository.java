package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.AcademicSession;
import examination.teacherAndStudents.entity.StudentTerm;
import examination.teacherAndStudents.utils.TermStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Repository
public interface StudentTermRepository extends JpaRepository<StudentTerm, Long> {
    StudentTerm findByNameAndAcademicSession(String name, AcademicSession academicSession);


    List<StudentTerm> findByAcademicSession(AcademicSession academicSession);



    boolean existsByNameAndAcademicSession(String name, AcademicSession session);

    boolean existsByNameAndAcademicSessionAndIdNot(String name, AcademicSession session, Long id);

    @Query("SELECT t FROM StudentTerm t " +
            "WHERE t.academicSession.status = 'ACTIVE' " +
            "AND t.academicSession.school.id = :schoolId " +
            "AND t.startDate <= :currentDate " +
            "AND t.endDate >= :currentDate " )
    Optional<StudentTerm> findCurrentTerm(@Param("currentDate") LocalDate currentDate, @Param("schoolId") Long schoolId);

    Optional<StudentTerm> findByIdAndAcademicSessionId(Long termId, Long id);
}

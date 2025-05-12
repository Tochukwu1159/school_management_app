package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Score;
import examination.teacherAndStudents.entity.Subject;
import examination.teacherAndStudents.utils.StudentTerm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    Subject findByName(String subject);
    @Query("SELECT s FROM Subject s WHERE (:name IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    List<Subject> findAllByFilters(@Param("name") String name);

    Page<Subject> findAllByNameContaining(String s, Pageable pageable);

    @Query("SELECT COUNT(cs) > 0 FROM ClassSubject cs WHERE cs.subject.id = :subjectId")
    boolean hasDependencies(Long subjectId);

    boolean existsByName(String trim);
}

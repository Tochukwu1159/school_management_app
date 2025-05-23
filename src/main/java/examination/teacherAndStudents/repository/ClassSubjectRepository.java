package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.ClassBlock;
import examination.teacherAndStudents.entity.ClassSubject;
import examination.teacherAndStudents.entity.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClassSubjectRepository extends JpaRepository<ClassSubject, Long> {


    Optional<ClassSubject> findByIdAndClassBlockId(Long id, Long studentClassId);

    @Query("SELECT cs FROM ClassSubject cs WHERE " +
            "(:subjectId IS NULL OR cs.subject.id = :subjectId) AND " +
            "(:classSubjectId IS NULL OR cs.id = :classSubjectId) AND " +
            "(:subClassId IS NULL OR cs.classBlock.id = :subClassId) AND " +

            "(:subjectName IS NULL OR LOWER(cs.subject.name) LIKE LOWER(CONCAT('%', :subjectName, '%')))")
    Page<ClassSubject> findAllWithFilters(
            @Param("subjectId") Long subjectId,
            @Param("classSubjectId") Long classSubjectId,
            @Param("subjectName") String subjectName,
            @Param("subClassId") Long subClassId,
            Pageable pageable);

    boolean existsBySubjectAndClassBlock(Subject subject, ClassBlock classBlock);
}

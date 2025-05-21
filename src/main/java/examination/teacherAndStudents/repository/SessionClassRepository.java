package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.SessionClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionClassRepository extends JpaRepository<SessionClass, Long> {

    @Query("SELECT s FROM SessionClass s WHERE " +
            "(:sessionId IS NULL OR s.academicSession.id = :sessionId) AND " +
            "(:classBlockId IS NULL OR s.classBlock.id = :classBlockId)")
    Page<SessionClass> findAllWithFilters(
            @Param("sessionId") Long sessionId,
            @Param("classBlockId") Long classBlockId,
            Pageable pageable);

    @Query("SELECT s FROM SessionClass s WHERE s.academicSession.id = :sessionId AND s.classBlock.id = :classBlockId")
    Optional<SessionClass> findBySessionIdAndClassBlockId(
            @Param("sessionId") Long sessionId,
            @Param("classBlockId") Long classBlockId);

    List<SessionClass> findByAcademicSessionId(Long sessionId);

}
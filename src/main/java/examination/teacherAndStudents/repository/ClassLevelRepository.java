package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.ClassLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClassLevelRepository extends JpaRepository<ClassLevel, Long> {

    @Query("SELECT cl FROM ClassLevel cl WHERE " +
            "(:classLevelId IS NULL OR cl.id = :classLevelId) AND " +
            "(:className IS NULL OR cl.className = :className) AND " +
            "(:schoolId IS NULL OR cl.school.id = :schoolId)")
    Page<ClassLevel> findAllWithFilters(
            @Param("classLevelId") Long classLevelId,
            @Param("className") String className,
            @Param("schoolId") Long schoolId,
            Pageable pageable);


    Optional<ClassLevel> findByClassNameIdAndSchoolId(Long id, Long id1);

    Optional<ClassLevel> findByIdAndSchoolId(Long classLevelId, Long id);
}

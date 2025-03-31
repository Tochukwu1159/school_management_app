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
    ClassLevel findByClassName(String classAssigned);

    @Query("SELECT cl FROM ClassLevel cl WHERE " +
            "(:classLevelId IS NULL OR cl.id = :classLevelId) AND " +
            "(:academicYearId IS NULL OR cl.academicYear.id = :academicYearId)")
    Page<ClassLevel> findAllWithFilters(
            @Param("classLevelId") Long classLevelId,
            @Param("academicYearId") Long academicYearId,
            Pageable pageable);

}

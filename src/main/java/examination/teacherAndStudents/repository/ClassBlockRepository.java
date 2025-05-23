package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.AcademicSession;
import examination.teacherAndStudents.entity.ClassBlock;
import examination.teacherAndStudents.entity.ClassLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface  ClassBlockRepository extends JpaRepository<ClassBlock, Long> {


    @Query("SELECT cb FROM ClassBlock cb WHERE " +
            "(:classLevelId IS NULL OR cb.classLevel.id = :classLevelId) AND " +
            "(:classBlockId IS NULL OR cb.id= :classBlockId)")
    List<ClassBlock> findAllWithFilters(
            @Param("classLevelId") Long classLevelId,
            @Param("classBlockId") Long classBlockId);

    Collection<ClassBlock> findByClassLevelId(Long id);

    List<ClassBlock> findByClassLevel(ClassLevel classLevel);

    Optional<ClassBlock> findByIdAndClassLevelId(Long id, Long classLevelId);

    Optional<ClassBlock> findByIdAndSchoolId(Long classBlockId, Long id);
};

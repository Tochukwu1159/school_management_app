package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.ClassBlock;
import examination.teacherAndStudents.entity.PromotionCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;


@Repository
public interface PromotionCriteriaRepository extends JpaRepository<PromotionCriteria, Long> {
    Optional<PromotionCriteria> findByClassBlock(ClassBlock currentClass);

    boolean existsByClassBlockId(Long classBlockId);

    Collection<PromotionCriteria> findByClassBlockId(Long classBlockId);
}

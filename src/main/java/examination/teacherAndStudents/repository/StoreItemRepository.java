package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.StoreItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreItemRepository extends JpaRepository<StoreItem, Long> {
    List<StoreItem> findBySchoolId(Long schoolId);

    Optional<StoreItem> findByIdAndSchoolId(Long itemId, Long id);
}

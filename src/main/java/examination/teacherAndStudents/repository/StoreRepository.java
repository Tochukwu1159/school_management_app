package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.StoreItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreRepository extends JpaRepository<StoreItem, Long> {
    List<StoreItem> findBySchoolId(Long schoolId);
}

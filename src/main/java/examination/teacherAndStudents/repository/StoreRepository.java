package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {
    List<Store> findBySchoolId(Long schoolId);

    Optional<Store> findByIdAndSchoolId(Long storeId, Long schoolId);
}

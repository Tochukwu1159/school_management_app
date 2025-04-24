package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.ServiceOffered;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceOfferedRepository extends JpaRepository<ServiceOffered, Long> {
    boolean existsByName(String name);

    List<ServiceOffered> findByIsDefaultTrue();

    @Query("SELECT s FROM ServiceOffered s WHERE " +
            "(:name IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:isDefault IS NULL OR s.isDefault = :isDefault)")
    Page<ServiceOffered> findByNameAndIsDefault(
            @Param("name") String name,
            @Param("isDefault") Boolean isDefault,
            Pageable pageable
    );

}

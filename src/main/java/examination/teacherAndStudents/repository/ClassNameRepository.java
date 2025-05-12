package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.ClassName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClassNameRepository extends JpaRepository<ClassName, Long> {

    Page<ClassName> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Optional<Task> findByIdAndSchoolId(Long id, Long id1);

    Page<Task> findAllByAssignedByOrAssignedToAndSchoolId(Profile profile, Profile profile1, Long id, Pageable pageable);

    Page<Task> findAllBySchoolId(Long id, Pageable pageable);
}

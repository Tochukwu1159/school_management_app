package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.AcademicSession;
import examination.teacherAndStudents.entity.ClassBlock;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.utils.Roles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findUserByRoles(Roles roles);
    Boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    User findByIdAndRoles(Long studentId, Roles roles);;
    Page<User> findAllByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCaseOrId(String firstName, String lastName, Long id, Pageable pageable);

    User findByEmailAndRoles(String email, Roles roles);
}

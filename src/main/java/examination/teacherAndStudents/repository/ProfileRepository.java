package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.AcademicSession;
import examination.teacherAndStudents.entity.ClassBlock;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByUser(User user);
    List<Profile> findByClassBlockId(Long classId);
    Optional<Profile> findByUniqueRegistrationNumber(String uniqueRegistrationNumber);
    Optional<Profile> findByUserId(Long studentId);
    Boolean existsByUniqueRegistrationNumber(String studentReg);

    Page<Profile> findAllByClassBlock(ClassBlock subClass,Pageable paging);
}
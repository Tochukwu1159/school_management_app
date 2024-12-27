package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.dto.UserProfileResponse;
import examination.teacherAndStudents.entity.AcademicSession;
import examination.teacherAndStudents.entity.ClassBlock;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.utils.ProfileStatus;
import examination.teacherAndStudents.utils.Roles;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

    List<Profile> findAllByClassBlock(ClassBlock subClass);

    List<Profile> findByClassBlock(ClassBlock classLevel);

    @Query("""
           SELECT new examination.teacherAndStudents.dto.UserProfileResponse(
               p.id, p.uniqueRegistrationNumber, p.phoneNumber, u.roles
           )
           FROM Profile p
           JOIN p.user u
           WHERE u.roles = :role AND p.profileStatus = :status
           """)
    Page<UserProfileResponse> findProfilesByRoleAndStatus(@Param("role") Roles role,
                                                          @Param("status") ProfileStatus status,
                                                          Pageable pageable);

    List<Profile> findAllByClassBlockClassLevelClassNameAndProfileStatus(String className, ProfileStatus profileStatus);
}
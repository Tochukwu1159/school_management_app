package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.School;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.utils.ProfileStatus;
import examination.teacherAndStudents.utils.Roles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Check if user exists by email
    Boolean existsByEmail(String email);

    // Find user by email (case-sensitive)
    Optional<User> findByEmail(String email);

    // Find users by a single role
    @Query("SELECT u FROM User u WHERE :role MEMBER OF u.roles")
    List<User> findUsersByRole(@Param("role") Roles role);

    // Find user by email and a specific role
    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email) AND :role MEMBER OF u.roles")
    Optional<User> findByEmailAndRole(@Param("email") String email, @Param("role") Roles role);

    // Find user by ID and a specific role
    @Query("SELECT u FROM User u WHERE u.id = :id AND :role MEMBER OF u.roles")
    Optional<User> findByIdAndRole(@Param("id") Long id, @Param("role") Roles role);

    // Find students with filters (for pagination)
    @Query("SELECT u FROM User u WHERE u.school.id = :schoolId " +
            "AND :studentRole MEMBER OF u.roles " +
            "AND (:firstName IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) " +
            "AND (:lastName IS NULL OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) " +
            "AND (:middleName IS NULL OR LOWER(u.middleName) LIKE LOWER(CONCAT('%', :middleName, '%'))) " +
            "AND (:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) " +
            "AND (:profileStatus IS NULL OR u.profileStatus = :profileStatus) " +
            "AND (:id IS NULL OR u.id = :id)")
    Page<User> findAllStudentsWithFilters(
            @Param("schoolId") Long schoolId,
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("middleName") String middleName,
            @Param("email") String email,
            @Param("profileStatus") ProfileStatus profileStatus,
            @Param("id") Long id,
            Pageable pageable);

    // Find users by role and school ID
    @Query("SELECT u FROM User u WHERE :role MEMBER OF u.roles AND u.school.id = :schoolId")
    List<User> findByRoleAndSchoolId(@Param("role") Roles role, @Param("schoolId") Long schoolId);

    // Find users by school and any of multiple roles
    @Query("SELECT u FROM User u WHERE u.school = :school AND EXISTS " +
            "(SELECT 1 FROM u.roles r WHERE r IN :roles)")
    List<User> findBySchoolAndAnyRoles(@Param("school") School school, @Param("roles") Set<Roles> roles);

    // Find users by school ID and profile status
    List<User> findBySchoolIdAndProfileStatus(@Param("schoolId") Long schoolId, @Param("profileStatus") ProfileStatus profileStatus);

    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email) AND EXISTS " +
            "(SELECT 1 FROM u.roles r WHERE r IN :roles)")
    Optional<User> findByEmailAndRolesIn(@Param("email") String email, @Param("roles") Set<Roles> roles);

    Optional<User> findByIdAndSchoolId(Long staffId, Long id);

    List<User> findBySchoolIdAndRoles(Long schoolId, Roles roleEnum);

    List<User> findBySchoolIdAndRolesNotIn(Long schoolId, List<Roles> excludedRoles);
}
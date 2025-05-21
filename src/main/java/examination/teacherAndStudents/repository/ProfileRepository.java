package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.dto.UserProfileResponse;
import examination.teacherAndStudents.entity.*;
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
    List<Profile> findBySessionClassId(Long classId);
    Optional<Profile> findByUniqueRegistrationNumber(String uniqueRegistrationNumber);
    Optional<Profile> findByUserId(Long studentId);
    Boolean existsByUniqueRegistrationNumber(String studentReg);


    @Query("""
           SELECT p
           FROM Profile p
           JOIN p.user u
           WHERE u.roles = :role AND p.profileStatus = :status
           """)
    Page<UserProfileResponse> findProfilesByRoleAndStatus(@Param("role") Roles role,
                                                          @Param("status") ProfileStatus status,
                                                          Pageable pageable);

    @Query("SELECT p FROM Profile p WHERE " +
            "(:sessionClass IS NULL OR p.sessionClass = :sessionClass) AND " +
            "(:classLevel IS NULL OR p.sessionClass.classBlock.classLevel = :classLevel) AND " +
            "(:academicYear IS NULL OR p.sessionClass.academicSession = :academicYear) AND " +
            "(:uniqueRegistrationNumber IS NULL OR p.uniqueRegistrationNumber LIKE %:uniqueRegistrationNumber%) AND " +
            "(:firstName IS NULL OR p.user.firstName LIKE %:firstName%) AND " +
            "(:lastName IS NULL OR p.user.lastName LIKE %:lastName%)")
    Page<Profile> findAllWithFilters(
            @Param("sessionClass") SessionClass sessionClass,
            @Param("classLevel") ClassLevel classLevel,
            @Param("academicYear") AcademicSession academicYear,
            @Param("uniqueRegistrationNumber") String uniqueRegistrationNumber,
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            Pageable pageable);

    List<Profile> findByUserIdIn(List<Long> teacherIds);

    Optional<Profile> findByUserEmail(String email);

    @Query("SELECT p FROM Profile p WHERE p.sessionClass.id IN :sessionClassIds AND " +
            "p.sessionClass.academicSession = :academicSession AND " +
            "p.sessionClass.classBlock.classLevel.school = :school AND " +
            "p.profileStatus = :profileStatus")
    List<Profile> findBySessionClassIdInAndSessionClassAcademicSessionAndSessionClassClassBlockClassLevelSchoolAndProfileStatus(
            @Param("sessionClassIds") List<Long> sessionClassIds,
            @Param("academicSession") AcademicSession academicSession,
            @Param("school") School school,
            @Param("profileStatus") ProfileStatus profileStatus);
    boolean existsByReferralCode(String code);

    Optional<Profile> findByReferralCode(String referralCode);

    List<Profile> findByUserIn(List<User> activeUsers);
}
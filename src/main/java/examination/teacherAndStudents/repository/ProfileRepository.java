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
    List<Profile> findByClassBlockId(Long classId);
    Optional<Profile> findByUniqueRegistrationNumber(String uniqueRegistrationNumber);
    Optional<Profile> findByUserId(Long studentId);
    Boolean existsByUniqueRegistrationNumber(String studentReg);

    Page<Profile> findAllByClassBlock(ClassBlock subClass,Pageable paging);

    List<Profile> findAllByClassBlock(ClassBlock subClass);

    List<Profile> findByClassBlock(ClassBlock classLevel);

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
            "(:classBlock IS NULL OR p.classBlock = :classBlock) AND " +
            "(:classLevel IS NULL OR p.classBlock.classLevel = :classLevel) AND " +
            "(:academicYear IS NULL OR p.classBlock.classLevel.academicYear = :academicYear) AND " +
            "(:uniqueRegistrationNumber IS NULL OR p.uniqueRegistrationNumber LIKE %:uniqueRegistrationNumber%) AND " +
            "(:firstName IS NULL OR p.user.firstName LIKE %:firstName%) AND " +
            "(:lastName IS NULL OR p.user.lastName LIKE %:lastName%)")

    Page<Profile> findAllWithFilters(
            @Param("classBlock") ClassBlock classBlock,
            @Param("classLevel") ClassLevel classLevel,
            @Param("academicYear") AcademicSession academicYear,
            @Param("uniqueRegistrationNumber") String uniqueRegistrationNumber,
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            Pageable pageable);

    List<Profile> findByUserIdIn(List<Long> teacherIds);

    Optional<Profile> findByUserEmail(String email);

    List<Profile> findByClassBlockAndClassBlock_ClassLevel_AcademicYear(ClassBlock classBlock, AcademicSession academicYear);


    List<Profile> findByClassBlockIdInAndClassBlockClassLevelAcademicYearAndClassBlockClassLevelSchoolAndProfileStatus(List<Long> classBlockIds, AcademicSession session, School school, ProfileStatus profileStatus);

    boolean existsByReferralCode(String code);

    Optional<Profile> findByReferralCode(String referralCode);

    List<Profile> findByUserIn(List<User> activeUsers);
}
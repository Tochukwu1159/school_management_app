package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Attendance;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.utils.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import examination.teacherAndStudents.entity.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByUserProfileAndDateBetween(Profile student, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT a FROM Attendance a WHERE " +
            "(:profileId IS NULL OR a.userProfile.id = :profileId) AND " +
            "(:academicYearId IS NULL OR a.academicYear.id = :academicYearId) AND " +
            "(:studentTermId IS NULL OR a.studentTerm.id = :studentTermId) AND " +
            "(:classBlockId IS NULL OR a.classBlock.id = :classBlockId) AND " +
            "(:startDate IS NULL OR a.date >= :startDate) AND " +
            "(:endDate IS NULL OR a.date <= :endDate) AND " +
            "(:createdAt IS NULL OR a.createdAt >= :createdAt)")
    Page<Attendance> findAllWithFilters(
            @Param("profileId") Long profileId,
            @Param("academicYearId") Long academicYearId,
            @Param("studentTermId") Long studentTermId,
            @Param("classBlockId") Long classBlockId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("createdAt") LocalDateTime createdAt,
            Pageable pageable);


    List<Attendance> findByUserProfileInAndDateBetween(Collection<Profile> userProfile, LocalDateTime date, LocalDateTime date2);

    long countByUserProfileIdAndStudentTerm(Long profileId, StudentTerm studentTerm);

    long countByUserProfileIdAndStudentTermAndStatus(Long profileId, StudentTerm studentTerm, AttendanceStatus attendanceStatus);


    boolean existsByUserProfileAndDateAndAcademicYearAndStudentTerm(Profile studentProfile, LocalDateTime date, AcademicSession academicSession, StudentTerm studentTerm);

    @Query("SELECT a FROM Attendance a WHERE " +
            "(:userProfileId IS NULL OR a.userProfile.id = :userProfileId) AND " +
            "(:classBlockId IS NULL OR a.classBlock.id = :classBlockId) AND " +
            "(:academicYearId IS NULL OR a.academicYear.id = :academicYearId) AND " +
            "(:studentTermId IS NULL OR a.studentTerm.id = :studentTermId) AND " +
            "(:status IS NULL OR a.status = :status) AND " +
            "(:startDate IS NULL OR a.date >= :startDate) AND " +
            "(:endDate IS NULL OR a.date <= :endDate)")
    Page<Attendance> findAllWithFilters(
            @Param("userProfileId") Long userProfileId,
            @Param("classBlockId") Long classBlockId,
            @Param("academicYearId") Long academicYearId,
            @Param("studentTermId") Long studentTermId,
            @Param("status") AttendanceStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    long countByUserProfileAndStatusAndDateBetween(
            Profile userProfile,
            AttendanceStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.userProfile.id = :userProfileId AND a.studentTerm = :studentTerm AND a.date BETWEEN :startDate AND :endDate")
    long countByUserProfileIdAndStudentTermAndDateRange(Long userProfileId, StudentTerm studentTerm, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.userProfile.id = :userProfileId AND a.studentTerm = :studentTerm AND a.status = :status AND a.date BETWEEN :startDate AND :endDate")
    long countByUserProfileIdAndStudentTermAndStatusAndDateRange(Long userProfileId, StudentTerm studentTerm, AttendanceStatus status, LocalDateTime startDate, LocalDateTime endDate);

}

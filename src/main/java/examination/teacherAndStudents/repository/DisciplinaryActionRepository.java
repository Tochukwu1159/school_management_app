package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.DisciplinaryAction;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.utils.DisciplinaryActionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DisciplinaryActionRepository extends JpaRepository<DisciplinaryAction, Long> {
    List<DisciplinaryAction> findByActiveTrueAndEndDateBefore(LocalDate date);

    @Query("SELECT da FROM DisciplinaryAction da WHERE da.active = true AND da.endDate < :currentDate")
    List<DisciplinaryAction> findExpiredActions(@Param("currentDate") LocalDate currentDate);


    List<DisciplinaryAction> findByProfileAndActiveTrue(Profile profile);

    @Query("SELECT CASE WHEN COUNT(da) > 0 THEN true ELSE false END " +
            "FROM DisciplinaryAction da " +
            "WHERE da.profile = :profile " +
            "AND da.actionType = :actionType " +
            "AND da.active = true " +
            "AND :currentDate BETWEEN da.startDate AND da.endDate")
    boolean existsActiveSuspensionForProfile(
            @Param("profile") Profile profile,
            @Param("actionType") DisciplinaryActionType actionType,
            @Param("currentDate") LocalDate currentDate);

    @Query("SELECT CASE WHEN COUNT(da) > 0 THEN true ELSE false END " +
            "FROM DisciplinaryAction da " +
            "WHERE da.profile = :profile " +
            "AND da.actionType = :actionType " +
            "AND da.active = true " +
            "AND :currentDate BETWEEN da.startDate AND da.endDate")
    boolean existsActiveActionForProfile(
            @Param("profile") Profile profile,
            @Param("actionType") DisciplinaryActionType actionType,
            @Param("currentDate") LocalDate currentDate);

    Page<DisciplinaryAction> findByActiveTrue(Pageable pageable);
}
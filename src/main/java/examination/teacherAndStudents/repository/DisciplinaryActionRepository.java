package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.DisciplinaryAction;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.utils.DisciplinaryActionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DisciplinaryActionRepository extends JpaRepository<DisciplinaryAction, Long> {
    List<DisciplinaryAction> findByProfileAndActiveTrue(Profile profile);

    @Query("SELECT da FROM DisciplinaryAction da WHERE da.profile = :profile AND da.actionType = :actionType " +
            "AND da.active = true AND da.startDate <= :date AND (da.endDate IS NULL OR da.endDate >= :date)")
    boolean existsActiveActionForProfile(
            @Param("profile") Profile profile,
            @Param("actionType") DisciplinaryActionType actionType,
            @Param("date") LocalDate date);

    List<DisciplinaryAction> findByActiveTrueAndEndDateBefore(LocalDate date);

    @Query("SELECT da FROM DisciplinaryAction da WHERE da.active = true " +
            "AND da.startDate <= :currentDate AND (da.endDate IS NULL OR da.endDate >= :currentDate)")
    Page<DisciplinaryAction> findByActiveTrueAndValidDate(
            @Param("currentDate") LocalDate currentDate,
            Pageable pageable);
}

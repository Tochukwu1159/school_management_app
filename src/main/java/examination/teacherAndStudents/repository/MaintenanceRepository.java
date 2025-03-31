package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Maintenance;
import examination.teacherAndStudents.entity.MedicalRecord;
import examination.teacherAndStudents.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenanceRepository extends JpaRepository<Maintenance, Long> {

    @Query("SELECT m FROM Maintenance m WHERE " +
            "m.transport.school.id = :schoolId AND " +
            "(:id IS NULL OR m.id = :id) AND " +
            "(:transportId IS NULL OR m.transport.id = :transportId) AND " +
            "(:maintainedById IS NULL OR m.maintainedBy.id = :maintainedById) AND " +
            "(:startDate IS NULL OR m.maintenanceDate >= :startDate) AND " +
            "(:endDate IS NULL OR m.maintenanceDate <= :endDate)")
    Page<Maintenance> findAllBySchoolWithFilters(
            @Param("schoolId") Long schoolId,
            @Param("id") Long id,
            @Param("transportId") Long transportId,
            @Param("maintainedById") Long maintainedById,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

}
package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Transport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransportRepository extends JpaRepository<Transport, Long> {

    @Query("SELECT t FROM Transport t WHERE " +
            "t.school.id = :schoolId AND " +
            "(:id IS NULL OR t.id = :id) AND " +
            "(:vehicleNumber IS NULL OR LOWER(t.vehicleNumber) LIKE LOWER(CONCAT('%', :vehicleNumber, '%'))) AND " +
            "(:licenceNumber IS NULL OR LOWER(t.licenceNumber) LIKE LOWER(CONCAT('%', :licenceNumber, '%'))) AND " +
            "(:driverId IS NULL OR t.driver.id = :driverId) AND " +
            "(:available IS NULL OR t.available = :available)")
    Page<Transport> findAllBySchoolWithFilters(
            @Param("schoolId") Long schoolId,
            @Param("id") Long id,
            @Param("vehicleNumber") String vehicleNumber,
            @Param("licenceNumber") String licenceNumber,
            @Param("driverId") Long driverId,
            @Param("available") Boolean available,
            Pageable pageable);

}

package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Bus;
import examination.teacherAndStudents.entity.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransportRepository extends JpaRepository<Bus, Long> {

    @Query("SELECT t FROM Bus t WHERE " +
            "t.school.id = :schoolId AND " +
            "(:id IS NULL OR t.busId = :id) AND " +
            "(:vehicleNumber IS NULL OR LOWER(t.vehicleNumber) LIKE LOWER(CONCAT('%', :vehicleNumber, '%'))) AND " +
            "(:licenceNumber IS NULL OR LOWER(t.licenceNumber) LIKE LOWER(CONCAT('%', :licenceNumber, '%'))) AND " +
            "(:driverId IS NULL OR t.driver.id = :driverId) AND " +
            "(:available IS NULL OR t.available = :available)")
    Page<Bus> findAllBySchoolWithFilters(
            @Param("schoolId") Long schoolId,
            @Param("id") Long id,
            @Param("vehicleNumber") String vehicleNumber,
            @Param("licenceNumber") String licenceNumber,
            @Param("driverId") Long driverId,
            @Param("available") Boolean available,
            Pageable pageable);

    Optional<Bus> findByBusIdAndSchoolId(Long transportId, Long id);

    Optional<Bus> findByDriver(Profile driver);

    boolean existsByVehicleNumber(String vehicleNumber);
    boolean existsByLicenceNumber(String licenceNumber);

}

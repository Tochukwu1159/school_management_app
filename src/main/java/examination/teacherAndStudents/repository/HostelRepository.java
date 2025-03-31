package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Hostel;
import examination.teacherAndStudents.utils.AvailabilityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface HostelRepository extends JpaRepository<Hostel, Long> {
    List<Hostel> findByAvailabilityStatus(AvailabilityStatus available);

    @Query("SELECT h FROM Hostel h WHERE " +
            "h.school.id = :schoolId AND " +
            "(:hostelName IS NULL OR LOWER(h.hostelName) LIKE LOWER(CONCAT('%', :hostelName, '%'))) AND " +
            "(:availabilityStatus IS NULL OR h.availabilityStatus = :availabilityStatus) AND " +
            "(:id IS NULL OR h.id = :id)")
    Page<Hostel> findAllBySchoolWithFilters(
            @Param("schoolId") Long schoolId,
            @Param("hostelName") String hostelName,
            @Param("availabilityStatus") AvailabilityStatus availabilityStatus,
            @Param("id") Long id,
            Pageable pageable);}

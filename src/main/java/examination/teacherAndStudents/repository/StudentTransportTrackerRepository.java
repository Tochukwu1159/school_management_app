package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.utils.AllocationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface StudentTransportTrackerRepository extends JpaRepository<StudentTransportAllocation, Long> {

    boolean existsByProfileAndTransportAndStatus(Profile student, Bus transport, AllocationStatus status);

    Optional<StudentTransportAllocation> findByProfileAndTransportAndStatus(Profile student, Bus transport, AllocationStatus allocationStatus);

    boolean existsByTransport(Bus transport);

    @Query("SELECT a FROM StudentTransportAllocation a WHERE a.profile = :profile " +
            "AND a.status = 'PENDING' " +
            "ORDER BY a.createdDate DESC")
    Optional<StudentTransportAllocation> findByProfile(Profile profile);


    @Query("SELECT sta FROM StudentTransportAllocation sta " +
            "WHERE sta.transport.driver.id = :driverId " +
            "AND sta.term.id = :termId " +
            "AND sta.status = :status")
    Page<StudentTransportAllocation> findByDriverIdAndTermIdAndStatus(
            @Param("driverId") Long driverId,
            @Param("termId") Long termId,
            @Param("status") AllocationStatus status,
            Pageable pageable);

    Optional<StudentTransportAllocation> findByProfileAndSchoolIdAndTerm(Profile student, Long id, StudentTerm currentTerm);

    Optional<StudentTransportAllocation> findByIdAndSchoolId(Long transportAllocationId, Long id);
}

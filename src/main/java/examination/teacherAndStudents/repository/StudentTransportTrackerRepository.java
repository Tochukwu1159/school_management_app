package examination.teacherAndStudents.repository;

import com.google.common.io.Files;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.utils.AllocationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
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
}

package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.StudentTransportAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentTransportAllocationRepository extends JpaRepository<StudentTransportAllocation, Long> {

    Optional<StudentTransportAllocation> findByProfileIdAndTransportBusId(Long id, Long busId);
}

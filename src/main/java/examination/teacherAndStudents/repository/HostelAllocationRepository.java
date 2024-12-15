package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Dues;
import examination.teacherAndStudents.entity.HostelAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HostelAllocationRepository extends JpaRepository<HostelAllocation, Long> {
    Optional<HostelAllocation> findByIdAndUserId(Long allocationId, Long userId);
    // You can add custom query methods if needed
}
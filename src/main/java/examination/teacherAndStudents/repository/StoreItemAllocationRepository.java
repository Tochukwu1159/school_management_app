package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.dto.StoreItemPaymentResponse;
import examination.teacherAndStudents.entity.StaffLevel;
import examination.teacherAndStudents.entity.StoreItemAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreItemAllocationRepository extends JpaRepository<StoreItemAllocation, Long> {
    List<StoreItemAllocation> findByProfileId(Long profileId);
}
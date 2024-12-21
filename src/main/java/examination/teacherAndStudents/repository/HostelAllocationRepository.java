package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.utils.AllocationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HostelAllocationRepository extends JpaRepository<HostelAllocation, Long> {
    HostelAllocation findByHostelAndBedNumberAndAcademicYear(Hostel hostel, int bedNumber, AcademicSession academicYear);

    HostelAllocation findByHostelAndProfileAndAcademicYearAndAllocationStatus(Hostel hostel, Profile userProfile, AcademicSession academicYear, AllocationStatus allocationStatus);

    HostelAllocation findByDuesIdAndProfile(Long dueId, Profile profile);

    HostelAllocation findByIdAndAcademicYearAndProfile(Long allocationId, AcademicSession academicYear, Profile userProfile);
    // You can add custom query methods if needed
}
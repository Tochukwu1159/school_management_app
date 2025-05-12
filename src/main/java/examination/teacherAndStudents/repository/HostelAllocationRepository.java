package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.utils.AllocationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface HostelAllocationRepository extends JpaRepository<HostelAllocation, Long> {

    boolean existsByHostel(Hostel hostel);

    boolean existsByProfileAndAcademicYearAndFee(Profile profile, AcademicSession academicSession, Fee fee);

    boolean existsByHostelAndProfileAndAcademicYearAndAllocationStatus(Hostel hostel, Profile profile, AcademicSession academicYear, AllocationStatus allocationStatus);

    boolean existsByHostelAndBedNumberAndAcademicYear(Hostel hostel, int bedNumber, AcademicSession academicYear);

    Collection<HostelAllocation> findBySchoolId(Long id);

    Optional<HostelAllocation> findByIdAndSchoolId(Long id, Long id1);
    // You can add custom query methods if needed
}
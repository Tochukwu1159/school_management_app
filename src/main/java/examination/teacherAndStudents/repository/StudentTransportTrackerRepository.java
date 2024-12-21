package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.utils.AllocationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentTransportTrackerRepository extends JpaRepository<StudentTransportAllocation, Long> {
    List<StudentTransportAllocation> findByProfile(Profile student);
    List<StudentTransportAllocation> findByTransport(Transport transport);

    boolean existsByProfileAndTransportAndStatus(Profile student, Transport transport, AllocationStatus status);

    StudentTransportAllocation findByDuesIdAndAcademicSessionAndTermAndProfile(Long dueId, AcademicSession session, StudentTerm term, Profile profile);

    Optional<StudentTransportAllocation> findByIdAndAcademicSessionAndTermAndProfile(Long transportTrackerId, AcademicSession academicSession, StudentTerm studentTerm, Profile profile);

    StudentTransportAllocation findByTransportAndProfileAndAcademicSessionAndStatus(Transport transport, Profile profile, AcademicSession academicSession, AllocationStatus allocationStatus);

}

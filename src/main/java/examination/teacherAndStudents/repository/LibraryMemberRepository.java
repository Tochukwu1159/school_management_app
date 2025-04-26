package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.LibraryMembership;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.utils.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LibraryMemberRepository extends JpaRepository<LibraryMembership, Long> {
    boolean existsByMemberId(String uniqueRegistrationNumber);

    boolean existsByStudentAndStatus(Profile student, MembershipStatus membershipStatus);

    List<LibraryMembership> findByStatusAndExpiryDateBefore(MembershipStatus membershipStatus, LocalDateTime now);

    Optional<LibraryMembership> findByStudentAndStatus(Profile profile, MembershipStatus membershipStatus);

    LibraryMembership findByMemberIdAndStatus(String memberId, MembershipStatus membershipStatus);
}
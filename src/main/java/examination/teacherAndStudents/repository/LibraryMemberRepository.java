package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.LibraryMembership;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibraryMemberRepository extends JpaRepository<LibraryMembership, Long> {
    boolean existsByMemberId(String uniqueRegistrationNumber);
    LibraryMembership findByMemberId(String memberId);

}
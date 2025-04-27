package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.AccountFunding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountFundingRepository extends JpaRepository<AccountFunding, Long> {
    Optional<AccountFunding> findByReference(String reference);
}

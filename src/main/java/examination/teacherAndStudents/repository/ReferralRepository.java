package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.Referral;
import examination.teacherAndStudents.utils.ReferralStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ReferralRepository extends JpaRepository<Referral, Long> {


    Referral findByReferredUserAndStatus(Profile profile, ReferralStatus referralStatus);
}
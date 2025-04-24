package examination.teacherAndStudents.repository;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.School;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.entity.Wallet;
import examination.teacherAndStudents.utils.WalletStatus;
import org.apache.commons.lang3.stream.Streams;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findWalletByUserProfile(Profile student);

    Optional<Wallet> findByUserProfile(Profile profile);


    List<Wallet> findBySchool(School school);

    List<Wallet> findBySchoolAndWalletStatus(School school, WalletStatus walletStatus);

    Optional<Wallet> findByUserProfileId(Long id);
}

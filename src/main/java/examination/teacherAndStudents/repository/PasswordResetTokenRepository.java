package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.PasswordResetToken;
import examination.teacherAndStudents.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    void deleteByResetToken(String token);

    PasswordResetToken findByUser(User user);
}

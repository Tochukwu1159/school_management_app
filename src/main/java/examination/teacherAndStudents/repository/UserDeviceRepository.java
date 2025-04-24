package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
    void deleteByFcmToken(String token);

    Optional<UserDevice> findByUserId(Long userId);

    Optional<UserDevice> findByDeviceId(String deviceId);

//    List<String> findAllFcmTokensByUserIds(List<Long> userIds);
}
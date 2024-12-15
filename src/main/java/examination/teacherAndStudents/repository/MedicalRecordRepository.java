package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.MedicalRecord;
import examination.teacherAndStudents.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    List<MedicalRecord> findAllByUser(Optional<User> student);
}
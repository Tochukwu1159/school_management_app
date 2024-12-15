package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Maintenance;
import examination.teacherAndStudents.entity.MedicalRecord;
import examination.teacherAndStudents.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenanceRepository extends JpaRepository<Maintenance, Long> {

}
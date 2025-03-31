package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.ScratchCard;
import examination.teacherAndStudents.entity.ScratchCardUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScratchCardUsageRepository extends JpaRepository<ScratchCardUsage, Long> {
    boolean existsByScratchCardAndStudent(ScratchCard card, Profile student);

    List<ScratchCardUsage> findByScratchCard(ScratchCard card);
}

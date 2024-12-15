package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    List<Notice> findByEventDateBetween(LocalDate startDate, LocalDate endDate);
}

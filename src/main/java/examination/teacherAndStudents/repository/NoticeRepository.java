package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    List<Notice> findByEventDateBetween(LocalDate startDate, LocalDate endDate);
}

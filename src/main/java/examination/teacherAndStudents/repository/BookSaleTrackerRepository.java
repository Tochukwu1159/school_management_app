package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.BookSaleTracker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookSaleTrackerRepository extends JpaRepository<BookSaleTracker, Long> {
    List<BookSaleTracker> findByProfileId(Long profileId);
}

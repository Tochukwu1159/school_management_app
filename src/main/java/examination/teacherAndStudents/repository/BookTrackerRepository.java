package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookTrackerRepository extends JpaRepository<BookTracker, Long> {

    Optional<BookTracker> findByBookSaleAndAcademicYear(BookSale book, AcademicSession academicSession);
}

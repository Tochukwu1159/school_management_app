package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Book;
import examination.teacherAndStudents.entity.BookBorrowing;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.utils.BorrowingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookBorrowingRepository extends JpaRepository<BookBorrowing, Long> {
    BookBorrowing findByStudentProfileAndBookAndStatusNot(Profile user, Book book, BorrowingStatus returned);


    @Query("SELECT COALESCE(SUM(b.fineAmount), 0) FROM BookBorrowing b " +
            "WHERE b.studentProfile = :profile AND b.status != 'RETURNED'")
    BigDecimal sumUnpaidFinesByProfile(@Param("profile") Profile profile);

    long countByStudentProfileAndStatusNot(Profile profile, BorrowingStatus borrowingStatus);

    List<BookBorrowing> findByStatusAndDueDateBefore(BorrowingStatus borrowingStatus, LocalDateTime now);

    List<BookBorrowing> findByStudentProfile(Profile profile);

    List<BookBorrowing> findByBook(Book book);

    boolean existsByBookAndStatusNot(Book book, BorrowingStatus borrowingStatus);

    boolean existsByStudentProfileAndStatusNot(Profile student, BorrowingStatus borrowingStatus);
}

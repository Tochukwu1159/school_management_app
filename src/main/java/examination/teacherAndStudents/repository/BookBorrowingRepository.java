package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Book;
import examination.teacherAndStudents.entity.BookBorrowing;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.utils.BorrowingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookBorrowingRepository extends JpaRepository<BookBorrowing, Long> {
    BookBorrowing findByStudentProfileAndBookAndStatusNot(Profile user, Book book, BorrowingStatus returned);
}

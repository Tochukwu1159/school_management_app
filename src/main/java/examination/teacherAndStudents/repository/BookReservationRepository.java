package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Book;
import examination.teacherAndStudents.entity.BookReservation;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.utils.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookReservationRepository extends JpaRepository<BookReservation, Long> {

    boolean existsByStudentProfileAndBookAndStatus(Profile profile, Book book, ReservationStatus reservationStatus);

    BookReservation findFirstByBookAndStatusOrderByReservationDateAsc(Book book, ReservationStatus reservationStatus);
}
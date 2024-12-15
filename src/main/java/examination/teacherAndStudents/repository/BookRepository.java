package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}

package examination.teacherAndStudents.repository;
import examination.teacherAndStudents.entity.Book;
import examination.teacherAndStudents.entity.BookSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookSaleRepository extends JpaRepository<BookSale, Long> {
}

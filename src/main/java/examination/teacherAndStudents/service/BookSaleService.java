package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.BookAssignmentRequest;
import examination.teacherAndStudents.entity.BookSale;

import java.util.List;

public interface BookSaleService {
    BookSale getBookById(Long id);
    List<BookSale> getAllBooks();
    BookSale createBookSale(String title, String author, String idNo, double price, Long classId, Long subjectId, int numberOfCopies);
}

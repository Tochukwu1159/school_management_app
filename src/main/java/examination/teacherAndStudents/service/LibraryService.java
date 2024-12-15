package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.BookRequest;
import examination.teacherAndStudents.entity.Book;
import examination.teacherAndStudents.entity.BookBorrowing;
import org.springframework.data.domain.Page;

import java.util.List;

public interface LibraryService {
    Book addBook(BookRequest book);
    Book updateBookQuantity(Long bookId, int quantityToAdd);

    Book editBook(Long bookId, BookRequest updatedBook);

    void deleteBook(Long bookId);

    Page<Book> getAllBooks(int pageNo, int pageSize, String sortBy);
    BookBorrowing borrowBook(String memberId, Long bookId);



    BookBorrowing returnBook(Long borrowingId);
}


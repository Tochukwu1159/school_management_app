package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.BookRequest;
import examination.teacherAndStudents.dto.BookResponse;
import examination.teacherAndStudents.dto.BookBorrowingResponse;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

public interface LibraryService {
    BookResponse addBook(BookRequest book);
    BookResponse updateBookQuantity(Long bookId, int quantityToAdd);
    BookResponse editBook(Long bookId, BookRequest updatedBook);
    void deleteBook(Long bookId);
    Page<BookResponse> getAllBooks(
            Long id,
            String title,
            String author,
            String shelfLocation,
            LocalDateTime createdAt,
            int pageNo,
            int pageSize,
            String sortBy,
            String sortDirection);
    BookBorrowingResponse borrowBook(Long bookId, LocalDateTime dueDate);
    BookBorrowingResponse returnBook(Long borrowingId);
}
package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.BookRequest;
import examination.teacherAndStudents.dto.BookResponse;
import examination.teacherAndStudents.entity.Book;
import examination.teacherAndStudents.entity.BookBorrowing;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public interface LibraryService {
    Book addBook(BookRequest book);
    Book updateBookQuantity(Long bookId, int quantityToAdd);

    Book editBook(Long bookId, BookRequest updatedBook);

    void deleteBook(Long bookId);

    Page<BookResponse> getAllBooks(
            Long id,
            String title,
            String author,
            LocalDateTime createdAt,
            int pageNo,
            int pageSize,
            String sortBy,
            String sortDirection);

    BookBorrowing borrowBook(Long memberId, Long bookId,LocalDateTime dueDate);



    BookBorrowing returnBook(Long borrowingId);
}


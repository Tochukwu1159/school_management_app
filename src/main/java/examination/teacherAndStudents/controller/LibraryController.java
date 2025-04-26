package examination.teacherAndStudents.controller;
import examination.teacherAndStudents.dto.BookRequest;
import examination.teacherAndStudents.dto.BookResponse;
import examination.teacherAndStudents.entity.Book;
import examination.teacherAndStudents.entity.BookBorrowing;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.service.LibraryService;
import examination.teacherAndStudents.utils.AccountUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/library")
public class LibraryController {

    private final LibraryService libraryService;

    @PostMapping("/addBook")
    public ResponseEntity<Book> addBook(@RequestBody BookRequest book) {
        try {
            Book addedBook = libraryService.addBook(book);
            return ResponseEntity.ok(addedBook);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/editBook/{bookId}")
    public ResponseEntity<Book> editBook(@PathVariable Long bookId, @RequestBody BookRequest updatedBook) {
        try {
            Book editedBook = libraryService.editBook(bookId, updatedBook);
            return ResponseEntity.ok(editedBook);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/deleteBook/{bookId}")
    public ResponseEntity<String> deleteBook(@PathVariable Long bookId) {
        try {
            libraryService.deleteBook(bookId);
            return ResponseEntity.ok("Book deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting book: " + e.getMessage());
        }
    }

    @GetMapping("/all_books")
    public ResponseEntity<Page<BookResponse>> getAllBooks(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String shelfLocation,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAt,
            @RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        try {
            Page<BookResponse> response = libraryService.getAllBooks(
                    id,
                    title,
                    author,
                    shelfLocation,
                    createdAt,
                    pageNo,
                    pageSize,
                    sortBy,
                    sortDirection);

            return ResponseEntity.ok(response);
        } catch (CustomNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @PostMapping("/borrow_book")
    public ResponseEntity<BookBorrowing> borrowBook( @RequestParam Long bookId, @RequestParam LocalDateTime dueDate) {
        try {
            BookBorrowing borrowing = libraryService.borrowBook(bookId, dueDate);
            return ResponseEntity.ok(borrowing);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/returnBook/{borrowingId}")
    public ResponseEntity<BookBorrowing> returnBook(@PathVariable Long borrowingId) {
        try {
            BookBorrowing returnedBook = libraryService.returnBook(borrowingId);
            return ResponseEntity.ok(returnedBook);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/updateBookQuantity/{bookId}")
    public ResponseEntity<Book> updateBookQuantity(
            @PathVariable Long bookId,
            @RequestParam int quantityToAdd) {
        try {
            Book updatedBook = libraryService.updateBookQuantity(bookId, quantityToAdd);
            return ResponseEntity.ok(updatedBook);
        } catch (Exception e) {
            throw new RuntimeException("Error updating book quantity: " + e.getMessage());
        }
    }
}


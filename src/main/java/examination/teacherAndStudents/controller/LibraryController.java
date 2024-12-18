package examination.teacherAndStudents.controller;
import examination.teacherAndStudents.dto.BookRequest;
import examination.teacherAndStudents.entity.Book;
import examination.teacherAndStudents.entity.BookBorrowing;
import examination.teacherAndStudents.service.LibraryService;
import examination.teacherAndStudents.utils.AccountUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Page<Book>> getAllBooks(@RequestParam(defaultValue = AccountUtils.PAGENO) Integer pageNo,
                                                  @RequestParam(defaultValue = AccountUtils.PAGESIZE) Integer pageSize,
                                                  @RequestParam(defaultValue = "id") String sortBy) {
        try {
            Page<Book> allBooks = libraryService.getAllBooks(pageNo, pageSize, sortBy);
            return ResponseEntity.ok(allBooks);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/borrow_book")
    public ResponseEntity<BookBorrowing> borrowBook(@RequestParam("studentId") Long memberId, @RequestParam Long bookId) {
        try {
            BookBorrowing borrowing = libraryService.borrowBook(memberId, bookId);
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


package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.BookRequest;
import examination.teacherAndStudents.dto.BookResponse;
import examination.teacherAndStudents.dto.BookBorrowingResponse;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.service.LibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/library")
public class LibraryController {

    private final LibraryService libraryService;

    @PostMapping("/addBook")
    public ResponseEntity<ApiResponse<BookResponse>> addBook(@RequestBody BookRequest book) {
        try {
            BookResponse addedBook = libraryService.addBook(book);
            return ResponseEntity.ok(new ApiResponse<>("Book added successfully", true, addedBook));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(e.getMessage(), false));
        }
    }

    @PutMapping("/editBook/{bookId}")
    public ResponseEntity<ApiResponse<BookResponse>> editBook(@PathVariable Long bookId, @RequestBody BookRequest updatedBook) {
        try {
            BookResponse editedBook = libraryService.editBook(bookId, updatedBook);
            return ResponseEntity.ok(new ApiResponse<>("Book updated successfully", true, editedBook));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>( e.getMessage(), false));
        }
    }

    @DeleteMapping("/deleteBook/{bookId}")
    public ResponseEntity<ApiResponse<String>> deleteBook(@PathVariable Long bookId) {
        try {
            libraryService.deleteBook(bookId);
            return ResponseEntity.ok(new ApiResponse<>("Book deleted successfully", true, "Book ID: " + bookId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>( e.getMessage(), false));
        }
    }

    @GetMapping("/all_books")
    public ResponseEntity<ApiResponse<Page<BookResponse>>> getAllBooks(
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
            return ResponseEntity.ok(new ApiResponse<>("Books retrieved successfully", true, response));
        } catch (CustomNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(  e.getMessage(), false));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>( e.getMessage(), false));
        }
    }

    @PostMapping("/borrow_book")
    public ResponseEntity<ApiResponse<BookBorrowingResponse>> borrowBook(@RequestParam Long bookId, @RequestParam LocalDateTime dueDate) {
        try {
            BookBorrowingResponse borrowing = libraryService.borrowBook(bookId, dueDate);
            return ResponseEntity.ok(new ApiResponse<>("Book borrowed successfully", true, borrowing));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(e.getMessage(), false));
        }
    }

    @PostMapping("/returnBook/{borrowingId}")
    public ResponseEntity<ApiResponse<BookBorrowingResponse>> returnBook(@PathVariable Long borrowingId) {
        try {
            BookBorrowingResponse returnedBook = libraryService.returnBook(borrowingId);
            return ResponseEntity.ok(new ApiResponse<>("Book returned successfully", true, returnedBook));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>( e.getMessage(), false));
        }
    }

    @PutMapping("/updateBookQuantity/{bookId}")
    public ResponseEntity<ApiResponse<BookResponse>> updateBookQuantity(
            @PathVariable Long bookId,
            @RequestParam int quantityToAdd) {
        try {
            BookResponse updatedBook = libraryService.updateBookQuantity(bookId, quantityToAdd);
            return ResponseEntity.ok(new ApiResponse<>("Book quantity updated successfully", true, updatedBook));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>( e.getMessage(), false));
        }
    }
}
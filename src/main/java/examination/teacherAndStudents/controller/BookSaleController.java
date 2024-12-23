package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.BookAssignmentRequest;
import examination.teacherAndStudents.dto.BookSaleRequest;
import examination.teacherAndStudents.dto.BookSaleResponse;
import examination.teacherAndStudents.entity.BookSale;
import examination.teacherAndStudents.service.BookSaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/book-sales")
@RequiredArgsConstructor
public class BookSaleController {

    private final BookSaleService bookSaleService;

    @GetMapping
    public ResponseEntity<List<BookSaleResponse>> getAllBooks() {
        return ResponseEntity.ok(bookSaleService.getAllBooks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookSaleResponse> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookSaleService.getBookById(id));
    }

    @PostMapping
    public ResponseEntity<BookSaleResponse> createBookSale(@RequestBody BookSaleRequest request) {
        BookSaleResponse response = bookSaleService.createBookSale(request);
        return ResponseEntity.ok(response);
    }
}

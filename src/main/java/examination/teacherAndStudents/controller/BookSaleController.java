package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.BookAssignmentRequest;
import examination.teacherAndStudents.entity.BookSale;
import examination.teacherAndStudents.service.BookSaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/book-sales")
@RequiredArgsConstructor
public class BookSaleController {

    private final BookSaleService bookSaleService;

    @GetMapping
    public ResponseEntity<List<BookSale>> getAllBooks() {
        return ResponseEntity.ok(bookSaleService.getAllBooks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookSale> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookSaleService.getBookById(id));
    }

    @PostMapping
    public ResponseEntity<BookSale> createBookSale(
            @RequestParam String title,
            @RequestParam String author,
            @RequestParam String idNo,
            @RequestParam double price,
            @RequestParam Long classId,
            @RequestParam Long subjectId,
            @RequestParam int numberOfCopies) {

        BookSale bookSale = bookSaleService.createBookSale(title, author, idNo, price, classId, subjectId, numberOfCopies);
        return ResponseEntity.ok(bookSale);
    }
}

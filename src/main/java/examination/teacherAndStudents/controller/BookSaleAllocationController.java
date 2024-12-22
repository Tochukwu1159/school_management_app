package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.entity.BookSaleAllocation;
import examination.teacherAndStudents.service.BookSaleAllocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/book-sale-tracker")
@RequiredArgsConstructor
public class BookSaleAllocationController {

    private final BookSaleAllocationService trackerService;

    @GetMapping
    public ResponseEntity<List<BookSaleAllocation>> getAllPurchases() {
        return ResponseEntity.ok(trackerService.getAllPurchases());
    }

    @GetMapping("/profile/{profileId}")
    public ResponseEntity<List<BookSaleAllocation>> getPurchasesByProfile(@PathVariable Long profileId) {
        return ResponseEntity.ok(trackerService.getPurchasesByProfile(profileId));
    }

    @PostMapping("/track")
    public ResponseEntity<BookSaleAllocation> trackPurchase(
            @RequestParam Long bookId,
            @RequestParam Long academicYearId,
            @RequestParam Long termId,
    @RequestParam  Long bookPaymentId) {
        BookSaleAllocation tracker = trackerService.allocateBook(bookId,academicYearId,termId, bookPaymentId);
        return ResponseEntity.ok(tracker);
    }
}

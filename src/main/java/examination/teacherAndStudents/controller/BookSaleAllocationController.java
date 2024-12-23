package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.BookPaymentRequest;
import examination.teacherAndStudents.dto.BookPaymentResponse;
import examination.teacherAndStudents.entity.BookSaleAllocation;
import examination.teacherAndStudents.service.BookSaleAllocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/book-sale")
@RequiredArgsConstructor
public class BookSaleAllocationController {

    private final BookSaleAllocationService trackerService;

    @PostMapping("/pay")
    public ResponseEntity<BookPaymentResponse> payForBook(@RequestBody BookPaymentRequest paymentRequest) {
        BookPaymentResponse paymentResponse = trackerService.payForBook(
                paymentRequest.getBookIds(),
                paymentRequest.getStudentId(),
                paymentRequest.getAcademicYearId(),
                paymentRequest.getTermId()
        );

        return ResponseEntity.ok(paymentResponse);
    }

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
            @RequestParam Long bookAllocationId) {
        BookSaleAllocation tracker = trackerService.allocateBook(bookId, academicYearId, termId, bookAllocationId);
        return ResponseEntity.ok(tracker);
    }
}

package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.entity.BookSaleTracker;
import examination.teacherAndStudents.service.BookSaleTrackerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/book-sale-tracker")
@RequiredArgsConstructor
public class BookSaleTrackerController {

    private final BookSaleTrackerService trackerService;

    @GetMapping
    public ResponseEntity<List<BookSaleTracker>> getAllPurchases() {
        return ResponseEntity.ok(trackerService.getAllPurchases());
    }

    @GetMapping("/profile/{profileId}")
    public ResponseEntity<List<BookSaleTracker>> getPurchasesByProfile(@PathVariable Long profileId) {
        return ResponseEntity.ok(trackerService.getPurchasesByProfile(profileId));
    }

    @PostMapping("/track")
    public ResponseEntity<BookSaleTracker> trackPurchase(
            @RequestParam Long bookId,
            @RequestParam Long profileId,
            @RequestParam double amountPaid) {

        BookSaleTracker tracker = trackerService.trackPurchase(bookId, profileId, amountPaid);
        return ResponseEntity.ok(tracker);
    }
}

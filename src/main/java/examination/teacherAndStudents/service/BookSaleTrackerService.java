package examination.teacherAndStudents.service;

import examination.teacherAndStudents.entity.BookSaleTracker;

import java.util.List;

public interface BookSaleTrackerService {
    BookSaleTracker trackPurchase(Long bookId, Long profileId, double amountPaid);
    List<BookSaleTracker> getPurchasesByProfile(Long profileId);
    List<BookSaleTracker> getAllPurchases();
}

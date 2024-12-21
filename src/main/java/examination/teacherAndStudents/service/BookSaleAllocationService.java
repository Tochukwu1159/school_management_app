package examination.teacherAndStudents.service;

import examination.teacherAndStudents.entity.BookSaleAllocation;

import java.util.List;

public interface BookSaleAllocationService {
    BookSaleAllocation allocateBook(Long bookId, Long academicYearId, Long termId, Long studentId, Long bookPaymentId);
    List<BookSaleAllocation> getPurchasesByProfile(Long profileId);
    List<BookSaleAllocation> getAllPurchases();
}

package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.BookPaymentResponse;
import examination.teacherAndStudents.entity.BookSaleAllocation;

import java.util.List;

public interface BookSaleAllocationService {
    BookSaleAllocation allocateBook(Long bookId, Long academicYearId, Long termId, Long bookAllocationId);
    BookPaymentResponse payForBook(List<Long> bookIds, Long studentId, Long academicYearId, Long termId);
    List<BookSaleAllocation> getPurchasesByProfile(Long profileId);
    List<BookSaleAllocation> getAllPurchases();

}

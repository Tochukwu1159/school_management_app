
package examination.teacherAndStudents.service;
import examination.teacherAndStudents.dto.StoreItemPaymentResponse;
import examination.teacherAndStudents.entity.StoreItemAllocation;

import java.util.List;

public interface StoreItemAllocationService {
    StoreItemAllocation allocateStoreItem(Long bookId, Long academicYearId, Long termId, Long storeItemAllocationId);
    StoreItemPaymentResponse payForStoreItem(List<Long> bookIds, Long studentId, Long academicYearId, Long termId);
    List<StoreItemAllocation> getPurchasesByProfile(Long profileId);
    List<StoreItemAllocation> getAllPurchases();

}

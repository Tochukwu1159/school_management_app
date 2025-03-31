package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.StoreItemPaymentResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.StoreItemAllocationService;
import examination.teacherAndStudents.utils.PaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class StoreItemAllocationServiceImpl implements StoreItemAllocationService {

    private static final String STORE_ITEM_NOT_FOUND = "Store Item not found";
    private static final String STUDENT_NOT_FOUND = "Student not found";
    private static final String PROFILE_NOT_FOUND = "Profile not found";
    private static final String ACADEMIC_YEAR_NOT_FOUND = "Academic year not found";
    private static final String TERM_NOT_FOUND = "Student term not found";
    private static final String ALLOCATION_NOT_FOUND = "Store Item Allocation not found";
    private static final String INSUFFICIENT_ITEMS = "Insufficient items available for size: ";

    private final StoreItemAllocationRepository storeItemAllocationRepository;
    private final StoreItemRepository storeRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final AcademicSessionRepository academicSessionRepository;
    private final StudentTermRepository studentTermRepository;
    private final StoreItemTrackerRepository storeItemTrackerRepository;

    public StoreItemAllocationServiceImpl(
            StoreItemAllocationRepository storeItemAllocationRepository,
            StoreItemRepository storeRepository,
            ProfileRepository profileRepository,
            UserRepository userRepository,
            AcademicSessionRepository academicSessionRepository,
            StudentTermRepository studentTermRepository,
            StoreItemTrackerRepository storeItemTrackerRepository) {
        this.storeItemAllocationRepository = storeItemAllocationRepository;
        this.storeRepository = storeRepository;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.academicSessionRepository = academicSessionRepository;
        this.studentTermRepository = studentTermRepository;
        this.storeItemTrackerRepository = storeItemTrackerRepository;
    }

    @Override
    public StoreItemAllocation allocateStoreItem(Long bookId, Long academicYearId, Long termId, Long storeItemAllocationId) {
        log.info("Allocating store item with ID: {} for allocation ID: {}", bookId, storeItemAllocationId);

        StoreItemAllocation storeItemAllocation = validateBookAllocation(storeItemAllocationId);
        StoreItem item = validateStoreItem(bookId);
        AcademicSession academicYear = validateAcademicYear(academicYearId);
        StudentTerm studentTerm = validateStudentTerm(termId);

        StoreItemTracker storeItemTracker = storeItemTrackerRepository.findByStoreItemAndAcademicYear(item, academicYear)
                .orElseGet(() -> createNewStoreItemTracker(item, academicYear));

        validateAndUpdateItemQuantities(item, storeItemTracker);

        StoreItemAllocation allocation = buildStoreItemAllocation(storeItemAllocation, item, academicYear, studentTerm);

        return storeItemAllocationRepository.save(allocation);
    }

    @Override
    public StoreItemPaymentResponse payForStoreItem(List<Long> storeItemIds, Long studentId, Long academicYearId, Long termId) {
        log.info("Processing payment for store items: {} by student: {}", storeItemIds, studentId);

        List<StoreItem> storeItems = validateStoreItemsExist(storeItemIds);
        Profile profile = validateStudentProfile(studentId);
        AcademicSession academicYear = validateAcademicYear(academicYearId);
        StudentTerm studentTerm = validateStudentTerm(termId);

        double totalAmountPaid = calculateTotalAmountPaid(storeItems);

        StoreItemAllocation allocation = buildStoreItemAllocationForPayment(profile, academicYear, studentTerm, totalAmountPaid, storeItems);
        StoreItemAllocation savedAllocation = storeItemAllocationRepository.save(allocation);

        log.info("Successfully processed payment of {} for {} items", totalAmountPaid, storeItems.size());

        return buildPaymentResponse(savedAllocation, storeItems);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoreItemAllocation> getPurchasesByProfile(Long profileId) {
        log.info("Fetching purchases for profile ID: {}", profileId);
        return storeItemAllocationRepository.findByProfileId(profileId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoreItemAllocation> getAllPurchases() {
        log.info("Fetching all purchases");
        return storeItemAllocationRepository.findAll();
    }

    // Helper Methods
    private List<StoreItem> validateStoreItemsExist(List<Long> storeItemIds) {
        List<StoreItem> storeItems = storeRepository.findAllById(storeItemIds);

        if (storeItems.size() != storeItemIds.size()) {
            Set<Long> foundIds = storeItems.stream()
                    .map(StoreItem::getId)
                    .collect(Collectors.toSet());

            List<Long> missingIds = storeItemIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();

            throw new NotFoundException("Store items not found with IDs: " + missingIds);
        }
        return storeItems;
    }

    private StoreItem validateStoreItem(Long storeItemId) {
        return storeRepository.findById(storeItemId)
                .orElseThrow(() -> new NotFoundException(STORE_ITEM_NOT_FOUND));
    }

    private Profile validateStudentProfile(Long studentId) {
        User user = userRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException(STUDENT_NOT_FOUND));

        return profileRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException(PROFILE_NOT_FOUND));
    }

    private AcademicSession validateAcademicYear(Long academicYearId) {
        return academicSessionRepository.findById(academicYearId)
                .orElseThrow(() -> new NotFoundException(ACADEMIC_YEAR_NOT_FOUND));
    }

    private StudentTerm validateStudentTerm(Long termId) {
        return studentTermRepository.findById(termId)
                .orElseThrow(() -> new NotFoundException(TERM_NOT_FOUND));
    }

    private StoreItemAllocation validateBookAllocation(Long storeItemAllocationId) {
        return storeItemAllocationRepository.findById(storeItemAllocationId)
                .orElseThrow(() -> new NotFoundException(ALLOCATION_NOT_FOUND));
    }

    private double calculateTotalAmountPaid(List<StoreItem> storeItems) {
        return storeItems.stream()
                .map(StoreItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.CEILING)
                .doubleValue();
    }

    private StoreItemTracker createNewStoreItemTracker(StoreItem item, AcademicSession academicYear) {
        return storeItemTrackerRepository.save(
                StoreItemTracker.builder()
                        .storeItem(item)
                        .academicYear(academicYear)
                        .storeItemRemaining(0)
                        .build()
        );
    }

    private void validateAndUpdateItemQuantities(StoreItem item, StoreItemTracker storeItemTracker) {
        Map<String, Integer> sizes = item.getSizes();
        int remaining = storeItemTracker.getStoreItemRemaining();

        for (Map.Entry<String, Integer> entry : sizes.entrySet()) {
            String size = entry.getKey();
            Integer quantityRequired = entry.getValue();

            if (remaining < quantityRequired) {
                throw new NotFoundException(INSUFFICIENT_ITEMS + size);
            }

            remaining -= quantityRequired;
        }

        storeItemTracker.setStoreItemRemaining(remaining);
        storeItemTrackerRepository.save(storeItemTracker);
    }

    private StoreItemAllocation buildStoreItemAllocation(
            StoreItemAllocation existingAllocation,
            StoreItem item,
            AcademicSession academicYear,
            StudentTerm studentTerm) {

        return StoreItemAllocation.builder()
                .profile(existingAllocation.getProfile())
                .storeItems(List.of(item))
                .academicYear(academicYear)
                .storeId(item.getStore().getId())
                .studentTerm(studentTerm)
                .paymentStatus(PaymentStatus.SUCCESS)
                .build();
    }

    private StoreItemAllocation buildStoreItemAllocationForPayment(
            Profile profile,
            AcademicSession academicYear,
            StudentTerm studentTerm,
            double totalAmountPaid,
            List<StoreItem> storeItems) {

        return StoreItemAllocation.builder()
                .paymentStatus(PaymentStatus.SUCCESS)
                .academicYear(academicYear)
                .studentTerm(studentTerm)
                .amountPaid(totalAmountPaid)
                .profile(profile)
                .storeItems(storeItems)
                .build();
    }

    private StoreItemPaymentResponse buildPaymentResponse(
            StoreItemAllocation allocation,
            List<StoreItem> storeItems) {

        return StoreItemPaymentResponse.builder()
                .totalAmountPaid(allocation.getAmountPaid())
                .storeItems(storeItems)
                .build();
    }
}
package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.StoreItemPaymentResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.StoreItemAllocationService;
import examination.teacherAndStudents.utils.PaymentStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class StoreItemAllocationServiceImpl implements StoreItemAllocationService {
    private final StoreItemAllocationRepository storeItemAllocationRepository;
    private final StoreRepository storeRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final AcademicSessionRepository academicSessionRepository;
    private final StudentTermRepository studentTermRepository;
    private final StoreItemTrackerRepository storeItemTrackerRepository;


    public StoreItemAllocationServiceImpl(StoreItemAllocationRepository storeItemAllocationRepository, StoreRepository storeRepository, ProfileRepository profileRepository, UserRepository userRepository, AcademicSessionRepository academicSessionRepository, StudentTermRepository studentTermRepository, StoreItemTrackerRepository storeItemTrackerRepository) {
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
        StoreItemAllocation storeItemAllocation = validateBookAllocation(storeItemAllocationId);
        StoreItem item = validateStoreItem(bookId);
        AcademicSession academicYear = validateAcademicYear(academicYearId);
        StudentTerm studentTerm = validateStudentTerm(termId);


        StoreItemTracker storeItemTracker = storeItemTrackerRepository.findByStoreItemAndAcademicYear(item, academicYear)
                .orElseGet(() -> storeItemTrackerRepository.save(

                        StoreItemTracker.builder()
                                .storeItem(item)
                                .academicYear(academicYear)
                                .storeItemRemaining(0)
                                .build()
                ));

        // Check size-based allocation
        Map<Integer, Integer> sizes = item.getSizes();
        for (Map.Entry<Integer, Integer> entry : sizes.entrySet()) {
            Integer size = entry.getKey();
            Integer quantityRequired = entry.getValue();

            int remaining = storeItemTracker.getStoreItemRemaining();
            if (remaining < quantityRequired) {
                throw new NotFoundException("Insufficient items available for size: " + size);
            }

            // Update remaining quantity for each size
            storeItemTracker.setStoreItemRemaining(remaining - quantityRequired);
        }

        // Save updated tracker
        storeItemTrackerRepository.save(storeItemTracker);

        StoreItemAllocation allocation = StoreItemAllocation.builder()
                .profile(storeItemAllocation.getProfile())
                .storeItems(List.of(item))
                .academicYear(academicYear)
                .studentTerm(studentTerm)
                .paymentStatus(PaymentStatus.SUCCESS)
                .build();

        return storeItemAllocationRepository.save(allocation);
    }

    @Override
    public StoreItemPaymentResponse payForStoreItem(List<Long> storeItemIds, Long studentId, Long academicYearId, Long termId) {
        // Validate books
        List<StoreItem> storeItems = validateStoreItem(storeItemIds);

        // Validate student and profile
        Profile profile = validateStudentProfile(studentId);

        // Validate academic year and term
        AcademicSession academicYear = validateAcademicYear(academicYearId);
        StudentTerm studentTerm = validateStudentTerm(termId);

        // Calculate total amount
        double totalAmountPaid = calculateTotalAmountPaid(storeItems);

        // Create and save book allocation
        StoreItemAllocation allocation = StoreItemAllocation.builder()
                .paymentStatus(PaymentStatus.SUCCESS)
                .academicYear(academicYear)
                .studentTerm(studentTerm)
                .amountPaid(totalAmountPaid)
                .profile(profile)
                .storeItems(storeItems)
                .build();

        // Save allocation (books will be saved automatically due to CascadeType.PERSIST)

        StoreItemAllocation savedAllocation = storeItemAllocationRepository.save(allocation);

        return  StoreItemPaymentResponse.builder()
                .totalAmountPaid(savedAllocation.getAmountPaid())
                .storeItems(storeItems)
                .build();
    }

    @Override
    public List<StoreItemAllocation> getPurchasesByProfile(Long profileId) {
        return storeItemAllocationRepository.findByProfileId(profileId);    }

    @Override
    public List<StoreItemAllocation> getAllPurchases() {
        return storeItemAllocationRepository.findAll();
    }


    // Helper Methods
    private List<StoreItem> validateStoreItem(List<Long> storeItemIds) {
        List<StoreItem> storeItems = storeRepository.findAllById(storeItemIds);
        if (storeItems.isEmpty() || storeItems.size() != storeItemIds.size()) {
            throw new NotFoundException("One or more store items not found");
        }
        return storeItems;
    }

    private StoreItem validateStoreItem(Long storeItem) {
        return storeRepository.findById(storeItem)
                .orElseThrow(() -> new NotFoundException("Store Item not found"));
    }

    private Profile validateStudentProfile(Long studentId) {
        return profileRepository.findByUser(
                userRepository.findById(studentId)
                        .orElseThrow(() -> new NotFoundException("Student not found"))
        ).orElseThrow(() -> new NotFoundException("Profile not found"));
    }

    private AcademicSession validateAcademicYear(Long academicYearId) {
        return academicSessionRepository.findById(academicYearId)
                .orElseThrow(() -> new NotFoundException("Academic year not found"));
    }

    private StudentTerm validateStudentTerm(Long termId) {
        return studentTermRepository.findById(termId)
                .orElseThrow(() -> new NotFoundException("Student term not found"));
    }

    private StoreItemAllocation validateBookAllocation(Long storeItemAllocationId) {
        return storeItemAllocationRepository.findById(storeItemAllocationId)
                .orElseThrow(() -> new NotFoundException("Store Item Allocation not found"));
    }

    private double calculateTotalAmountPaid(List<StoreItem> storeItems) {
        return Math.ceil(storeItems.stream()
                .mapToDouble(StoreItem::getPrice)
                .sum());
    }

}

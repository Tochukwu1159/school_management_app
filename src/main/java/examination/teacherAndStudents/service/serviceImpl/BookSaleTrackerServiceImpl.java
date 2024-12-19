package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.entity.BookSale;
import examination.teacherAndStudents.entity.BookSaleTracker;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.repository.BookSaleRepository;
import examination.teacherAndStudents.repository.BookSaleTrackerRepository;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.service.BookSaleTrackerService;
import examination.teacherAndStudents.utils.PaymentStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookSaleTrackerServiceImpl implements BookSaleTrackerService {

    private final BookSaleTrackerRepository trackerRepository;
    private final BookSaleRepository bookSaleRepository;
    private final ProfileRepository profileRepository;

    public List<BookSaleTracker> getAllPurchases() {
        return trackerRepository.findAll();
    }

    public List<BookSaleTracker> getPurchasesByProfile(Long profileId) {
        return trackerRepository.findByProfileId(profileId);
    }

    @Transactional
    public BookSaleTracker trackPurchase(Long bookId, Long profileId, double amountPaid) {
        BookSale book = bookSaleRepository.findById(bookId)
                .orElseThrow(() -> new NotFoundException("Book not found"));

        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new NotFoundException("Profile not found"));

        BookSaleTracker tracker = BookSaleTracker.builder()
                .book(book)
                .profile(profile)
                .amountPaid(amountPaid)
                .paymentStatus(PaymentStatus.SUCCESS)
                .build();

        return trackerRepository.save(tracker);
    }
}

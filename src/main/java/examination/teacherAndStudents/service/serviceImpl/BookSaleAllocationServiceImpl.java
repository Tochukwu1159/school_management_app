package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.BookPaymentResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.BookSaleAllocationService;
import examination.teacherAndStudents.service.PaymentService;
import examination.teacherAndStudents.utils.PaymentStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookSaleAllocationServiceImpl implements BookSaleAllocationService {

    private final BookSaleAllocationRepository bookSaleAllocationRepository;
    private final BookSaleRepository bookSaleRepository;
    private final ProfileRepository profileRepository;
    private final BookTrackerRepository bookTrackerRepository;
    private final AcademicSessionRepository academicSessionRepository;
    private final StudentTermRepository studentTermRepository;
    private final UserRepository userRepository;
    public List<BookSaleAllocation> getAllPurchases() {
        return bookSaleAllocationRepository.findAll();
    }

    public List<BookSaleAllocation> getPurchasesByProfile(Long profileId) {
        return bookSaleAllocationRepository.findByProfileId(profileId);
    }

    @Transactional
    public BookPaymentResponse payForBook(List<Long> bookIds, Long studentId, Long academicYearId, Long termId) {
        // Validate books
        List<BookSale> books = validateBooks(bookIds);

        // Validate student and profile
        Profile profile = validateStudentProfile(studentId);

        // Validate academic year and term
        AcademicSession academicYear = validateAcademicYear(academicYearId);
        StudentTerm studentTerm = validateStudentTerm(termId);

        // Calculate total amount
        double totalAmountPaid = calculateTotalAmountPaid(books);

        // Create and save book allocation
        BookSaleAllocation allocation = BookSaleAllocation.builder()
                .paymentStatus(PaymentStatus.SUCCESS)
                .academicYear(academicYear)
                .studentTerm(studentTerm)
                .amountPaid(totalAmountPaid)
                .profile(profile)
                .books(books)
                .build();

        // Save allocation (books will be saved automatically due to CascadeType.PERSIST)
        BookSaleAllocation savedAllocation = bookSaleAllocationRepository.save(allocation);

        return BookPaymentResponse.builder()
                .totalAmountPaid(savedAllocation.getAmountPaid())
                .books(books)
                .build();
    }



    @Transactional
    public BookSaleAllocation allocateBook(Long bookId, Long academicYearId, Long termId, Long bookAllocationId) {
        BookSaleAllocation bookAllocation = validateBookAllocation(bookAllocationId);
        BookSale book = validateBook(bookId);
        AcademicSession academicYear = validateAcademicYear(academicYearId);
        StudentTerm studentTerm = validateStudentTerm(termId);

        BookTracker bookTracker = bookTrackerRepository.findByBookSaleAndAcademicYear(book, academicYear)
                .orElseGet(() -> bookTrackerRepository.save(
                        BookTracker.builder()
                                .bookSale(book)
                                .academicYear(academicYear)
                                .bookRemaining(0)
                                .build()
                ));

        if (bookTracker.getBookRemaining() < book.getNumberOfCopies()) {
            throw new NotFoundException("Insufficient books available for this academic year");
        }

        bookTracker.setBookRemaining(bookTracker.getBookRemaining() - book.getNumberOfCopies());
        bookTrackerRepository.save(bookTracker);

        BookSaleAllocation allocation = BookSaleAllocation.builder()
                .profile(bookAllocation.getProfile())
                .books(List.of(book))
                .paymentStatus(PaymentStatus.SUCCESS)
                .build();

        return bookSaleAllocationRepository.save(allocation);
    }

    // Helper Methods
    private List<BookSale> validateBooks(List<Long> bookIds) {
        List<BookSale> books = bookSaleRepository.findAllById(bookIds);
        if (books.isEmpty() || books.size() != bookIds.size()) {
            throw new NotFoundException("One or more books not found");
        }
        return books;
    }

    private BookSale validateBook(Long bookId) {
        return bookSaleRepository.findById(bookId)
                .orElseThrow(() -> new NotFoundException("Book not found"));
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

    private BookSaleAllocation validateBookAllocation(Long bookAllocationId) {
        return bookSaleAllocationRepository.findById(bookAllocationId)
                .orElseThrow(() -> new NotFoundException("Book Allocation not found"));
    }

    private double calculateTotalAmountPaid(List<BookSale> books) {
        return Math.ceil(books.stream()
                .mapToDouble(BookSale::getPrice)
                .sum());
    }

}

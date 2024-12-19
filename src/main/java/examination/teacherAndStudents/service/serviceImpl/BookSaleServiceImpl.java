
package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.BookAssignmentRequest;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.BookSaleService;
import examination.teacherAndStudents.service.BookSaleTrackerService;
import examination.teacherAndStudents.service.PaymentService;
import examination.teacherAndStudents.service.TransactionService;
import examination.teacherAndStudents.utils.PaymentStatus;
import examination.teacherAndStudents.utils.TransactionType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookSaleServiceImpl implements BookSaleService {

    private final BookSaleRepository bookSaleRepository;
    private final ClassLevelRepository classLevelRepository;
    private final SubjectRepository subjectRepository;
    private final ProfileRepository profileRepository;
    private final BookSaleTrackerRepository bookSaleTrackerRepository;
    private final PaymentService paymentService;
    private final TransactionService transactionService;
    private final AcademicSessionRepository academicSessionRepository;
    private final TransactionRepository transactionRepository;

    private final BookSaleTrackerService bookSaleTrackerService;

    public List<BookSale> getAllBooks() {
        return bookSaleRepository.findAll();
    }

    public BookSale getBookById(Long id) {
        return bookSaleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book not found"));
    }

    @Transactional
    public BookSale createBookSale(String title, String author, String idNo, double price, Long classId, Long subjectId, int numberOfCopies) {
        ClassLevel classLevel = classLevelRepository.findById(classId)
                .orElseThrow(() -> new NotFoundException("Class level not found"));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new NotFoundException("Subject not found"));

        BookSale bookSale = BookSale.builder()
                .title(title)
                .author(author)
                .idNo(idNo)
                .numberOfCopies(numberOfCopies)
                .price(price)
                .classLevel(classLevel)
                .subject(subject)
                .build();

        return bookSaleRepository.save(bookSale);
    }

    @Transactional
    public void assignBooksToStudent(BookAssignmentRequest request) {
        // Retrieve student profile
        Profile student = profileRepository.findById(request.getStudentId())
                .orElseThrow(() -> new NotFoundException("Student profile not found"));

        // Retrieve academic session
        AcademicSession academicSession = academicSessionRepository.findById(request.getSession())
                .orElseThrow(() -> new NotFoundException("Academic session not found"));

        // Fetch books by IDs
        List<BookSale> books = bookSaleRepository.findAllById(request.getBookIds());

        // Ensure all books are found
        if (books.size() != request.getBookIds().size()) {
            throw new NotFoundException("Some books not found");
        }

        for (BookSale book : books) {
            if (book.getNumberOfCopies() <= 0) {
                throw new IllegalArgumentException("Not enough copies for book: " + book.getTitle());
            }
        }

        // Calculate the total price
        double totalPrice = books.stream()
                .mapToDouble(BookSale::getPrice)
                .sum();

        // Check if the paid amount matches the total price
        if (totalPrice != request.getAmountPaid()) {
            throw new IllegalArgumentException("Incorrect amount. Total price: " + totalPrice);
        }

        // Assuming the payment is handled externally
        boolean paymentSuccessful = true; // Set this to true for now since payment logic is not implemented here
        if (!paymentSuccessful) {
            throw new CustomInternalServerException("Payment failed");
        }

        // Create a transaction record for the payment
        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.DEBIT)
                .user(student)
                .amount(BigDecimal.valueOf(totalPrice))
                .studentTerm(null)
                .session(academicSession)
                .description("You have successfully paid " + totalPrice + " for book purchases")
                .build();

        transactionRepository.save(transaction);

        // Assign books and track purchases
        for (BookSale book : books) {
            book.setNumberOfCopies(book.getNumberOfCopies() - 1);
            bookSaleRepository.save(book);
            // Create a BookSaleTracker entry instead of directly modifying the Profile
            BookSaleTracker tracker = BookSaleTracker.builder()
                    .book(book)
                    .profile(student)
                    .amountPaid(book.getPrice())
                    .paymentStatus(PaymentStatus.SUCCESS)
                    .build();

            bookSaleTrackerRepository.save(tracker);
        }
    }

}

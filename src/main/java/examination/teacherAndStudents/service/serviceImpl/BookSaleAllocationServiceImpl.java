package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.BookPaymentResponse;
import examination.teacherAndStudents.dto.PaymentResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.error_handler.PaymentFailedException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.BookSaleAllocationService;
import examination.teacherAndStudents.utils.AllocationStatus;
import examination.teacherAndStudents.utils.PaymentStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookSaleAllocationServiceImpl implements BookSaleAllocationService {

    private final BookSaleAllocationRepository trackerRepository;
    private final BookSaleRepository bookSaleRepository;
    private final ProfileRepository profileRepository;
    private final BookTrackerRepository bookTrackerRepository;
    private final AcademicSessionRepository academicSessionRepository;
    private final StudentTermRepository studentTermRepository;
    private final UserRepository userRepository;
    private final BookSaleAllocationRepository bookSaleAllocationRepository;

    public List<BookSaleAllocation> getAllPurchases() {
        return trackerRepository.findAll();
    }

    public List<BookSaleAllocation> getPurchasesByProfile(Long profileId) {
        return trackerRepository.findByProfileId(profileId);
    }

    @Transactional
    public BookPaymentResponse payForBook(List<Long> bookIds, Long studentId, Long academicYearId, Long termId) {
        // Validate if books exist
        List<BookSale> books = bookSaleRepository.findAllById(bookIds);
        if (books.isEmpty() || books.size() != bookIds.size()) {
            throw new NotFoundException("One or more books not found");
        }

        // Validate if student exists
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found"));

        // Get the student's profile
        Profile profile = profileRepository.findByUser(student)
                .orElseThrow(() -> new NotFoundException("Profile not found"));

        // Get the academic year and term
        AcademicSession academicYear = academicSessionRepository.findById(academicYearId)
                .orElseThrow(() -> new NotFoundException("Academic year not found"));

        StudentTerm studentTerm = studentTermRepository.findById(termId)
                .orElseThrow(() -> new NotFoundException("Term not found"));

        // Call the payment service (You should replace this with actual payment processing)
//        PaymentResponse paymentResponse = paymentService.processPayment(student, books);
//        if (paymentResponse.getStatus() != PaymentStatus.SUCCESS) {
//            throw new PaymentFailedException("Payment failed for student ID: " + studentId);
//        }

        // Calculate the total amount paid for all books
        double totalAmountPaid = books.stream()
                .mapToDouble(BookSale::getPrice)  // Assuming getPrice() is the method that returns the price of a book
                .sum();

        // Create a BookSaleAllocation entry and link multiple books to it
        BookSaleAllocation allocation = BookSaleAllocation.builder()
                .paymentStatus(PaymentStatus.SUCCESS)
                .amountPaid(totalAmountPaid)
                .profile(profile)
//                .paymentId(paymentResponse.getPaymentId())
                .build();

        // Add all books to the allocation (this assumes allocation has a setBooks() method)
        allocation.setBooks(books);

        // Save the allocation
        BookSaleAllocation savedAllocation = bookSaleAllocationRepository.save(allocation);

        // Return the payment response along with allocation details
        return BookPaymentResponse.builder()
//                .paymentId(savedAllocation.getPaymentId())
                .totalAmountPaid(savedAllocation.getAmountPaid())
//                .paymentStatus(savedAllocation.getPaymentStatus())
                .books(books)  // Include all the bookIds in the response
                .build();
    }


    @Transactional
    public BookSaleAllocation allocateBook(Long academicYearId, Long termId, Long bookAllocationId) {
        // Validate if the BookAllocation exists and retrieve it
        BookSaleAllocation bookAllocation = bookSaleAllocationRepository.findById(bookAllocationId)
                .orElseThrow(() -> new NotFoundException("Book Allocation not found with ID: " + bookAllocationId));

        // Get the books linked to the allocation
        List<BookSale> books = bookAllocation.getBooks();
        if (books.isEmpty()) {
            throw new NotFoundException("No books found in the allocation");
        }

        // Get the academic year and term
        AcademicSession academicSession = academicSessionRepository.findById(academicYearId)
                .orElseThrow(() -> new NotFoundException("Academic year not found with ID: " + academicYearId));

        StudentTerm studentTerm = studentTermRepository.findById(termId)
                .orElseThrow(() -> new NotFoundException("Student term not found with ID: " + termId));

        // Allocate books and check stock for each book
        List<BookTracker> bookTrackers = new ArrayList<>();
        for (BookSale book : books) {
            // Check if the book is available for the selected academic year
            BookTracker bookTracker = bookTrackerRepository.findByBookSaleAndAcademicYear(book, academicSession)
                    .orElseGet(() -> bookTrackerRepository.save(
                            BookTracker.builder()
                                    .bookSale(book)
                                    .academicYear(academicSession)
                                    .bookRemaining(0)
                                    .build()
                    ));

            // Check if enough books are available
            if (bookTracker.getBookRemaining() >= book.getNumberOfCopies()) {
                throw new NotFoundException("No books available for the selected academic year and book ID: " + book.getId());
            }

            // Update book tracker for each book
            bookTracker.setBookRemaining(bookTracker.getBookRemaining() + 1);
            bookTrackers.add(bookTracker);
        }

        // Save all the updated book trackers
        bookTrackerRepository.saveAll(bookTrackers);

        // Get the student's profile from the original allocation
        Profile profile = bookAllocation.getProfile();

        // Create a BookSaleAllocation and associate the books
        BookSaleAllocation allocation = BookSaleAllocation.builder()
                .paymentStatus(PaymentStatus.SUCCESS)
                .profile(profile)
//                .paymentId(bookAllocation.getPaymentId())  // Use bookPaymentId here
                .books(books) // Link the books to this allocation
                .build();

        // Save the allocation and return it
        return bookSaleAllocationRepository.save(allocation);
    }
    private double calculateTotalAmountPaid(List<BookSale> books) {
        return books.stream()
                .mapToDouble(BookSale::getPrice) // Assuming `BookSale` has a `getPrice` method
                .sum();
    }


    @Transactional
    public BookSaleAllocation allocateBook(Long bookId, Long academicYearId, Long termId, Long studentId, Long bookPaymentId) {
        BookSale book = bookSaleRepository.findById(bookId)
                .orElseThrow(() -> new NotFoundException("Book not found")); ///update thius method and remove student id


        AcademicSession academicSession = academicSessionRepository.findById(academicYearId)
                .orElseThrow(() -> new NotFoundException("Academic year not found with ID: " + academicYearId));

        StudentTerm studentTerm = studentTermRepository.findById(termId)
                .orElseThrow(() -> new NotFoundException("Student term not found with ID: " + studentId));

        // Find the student by ID
        User user = userRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found with ID: " + studentId));


        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("Profile not found"));


        BookTracker bookTracker = bookTrackerRepository.findByBookSaleAndAcademicYear(book, academicSession)
                .orElseGet(() -> bookTrackerRepository.save(
                        BookTracker.builder()
                                .bookSale(book)
                                .academicYear(academicSession)
                                .bookRemaining(0)
                                .build()
                ));

        if (bookTracker.getBookRemaining() >= book.getNumberOfCopies()) {
            throw new NotFoundException("No books available in this book id for the selected academic year");
        }

        bookTracker.setBookRemaining(bookTracker.getBookRemaining() + 1);
        bookTrackerRepository.save(bookTracker);

        BookSaleAllocation tracker = BookSaleAllocation.builder()
//                .book(book)
                .profile(profile)
                .paymentStatus(PaymentStatus.SUCCESS)
                .build();

        return trackerRepository.save(tracker);
    }
}

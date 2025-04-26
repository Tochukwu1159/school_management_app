package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.BookRequest;
import examination.teacherAndStudents.dto.BookResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.*;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.LibraryService;
import examination.teacherAndStudents.utils.BorrowingStatus;
import examination.teacherAndStudents.utils.MembershipStatus;
import examination.teacherAndStudents.utils.ReservationStatus;
import examination.teacherAndStudents.utils.Roles;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LibraryServiceImpl implements LibraryService {

    private static final Logger logger = LoggerFactory.getLogger(LibraryServiceImpl.class);

    private final BookRepository bookRepository;
    private final BookBorrowingRepository bookBorrowingRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final AuditLogRepository auditLogRepository;
    private final BookReservationRepository bookReservationRepository;
    private final LibraryMemberRepository libraryMemberRepository;

    @Override
    @Transactional
    public Book addBook(BookRequest bookRequest) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));
        Profile profile = profileRepository.findByUser(admin).orElseThrow(() -> new CustomNotFoundException("Profile not found"));

        if (bookRequest.getQuantityAvailable() <= 0) {
            throw new BadRequestException("Quantity must be positive");
        }

        if (bookRepository.existsByTitleAndAuthorAndSchool(
                bookRequest.getTitle(), bookRequest.getAuthor(), admin.getSchool())) {
            throw new EntityAlreadyExistException("Book already exists in this school");
        }

        Book newBook = Book.builder()
                .title(bookRequest.getTitle())
                .author(bookRequest.getAuthor())
                .shelfLocation(bookRequest.getShelfLocation())
                .quantityAvailable(bookRequest.getQuantityAvailable())
                .totalCopies(bookRequest.getQuantityAvailable())
                .school(admin.getSchool())
                .build();

        Book savedBook = bookRepository.save(newBook);

        auditLogRepository.save(
                AuditLog.builder()
                        .action("ADD_BOOK")
                        .performedBy(profile)
                        .timestamp(LocalDateTime.now())
                        .details("Book: " + newBook.getTitle())
                        .build()
        );

        logger.info("Added book: {}", newBook.getTitle());
        return savedBook;
    }

    @Override
    @Transactional
    public Book updateBookQuantity(Long bookId, int quantityToAdd) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));
        Profile profile = profileRepository.findByUser(admin).orElseThrow(() -> new CustomNotFoundException("Profile not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomNotFoundException("Book not found"));

        if (quantityToAdd < 0 && book.getQuantityAvailable() + quantityToAdd < 0) {
            throw new BadRequestException("Cannot reduce quantity below zero");
        }

        book.setQuantityAvailable(book.getQuantityAvailable() + quantityToAdd);
        book.setTotalCopies(book.getTotalCopies() + quantityToAdd);

        Book updatedBook = bookRepository.save(book);

        auditLogRepository.save(
                AuditLog.builder()
                        .action("UPDATE_BOOK_QUANTITY")
                        .performedBy(profile)
                        .timestamp(LocalDateTime.now())
                        .details("Book ID: " + bookId + ", Quantity Added: " + quantityToAdd)
                        .build()
        );

        logger.info("Updated quantity for book ID {}: {}", bookId, quantityToAdd);
        return updatedBook;
    }

    @Override
    @Transactional
    public Book editBook(Long bookId, BookRequest updatedBook) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));
        Profile profile = profileRepository.findByUser(admin).orElseThrow(() -> new CustomNotFoundException("Profile not found"));

        Book existingBook = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomNotFoundException("Book not found"));

        existingBook.setTitle(updatedBook.getTitle());
        existingBook.setAuthor(updatedBook.getAuthor());
        existingBook.setShelfLocation(updatedBook.getShelfLocation());
        existingBook.setQuantityAvailable(updatedBook.getQuantityAvailable());
        existingBook.setTotalCopies(updatedBook.getQuantityAvailable());

        Book savedBook = bookRepository.save(existingBook);

        auditLogRepository.save(
                AuditLog.builder()
                        .action("EDIT_BOOK")
                        .performedBy(profile)
                        .timestamp(LocalDateTime.now())
                        .details("Book ID: " + bookId + ", Title: " + updatedBook.getTitle())
                        .build()
        );

        logger.info("Edited book ID {}", bookId);
        return savedBook;
    }

    @Override
    @Transactional
    public void deleteBook(Long bookId) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));
        Profile profile = profileRepository.findByUser(admin).orElseThrow(() -> new CustomNotFoundException("Profile not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomNotFoundException("Book not found"));

        if (bookBorrowingRepository.existsByBookAndStatusNot(book, BorrowingStatus.RETURNED)) {
            throw new BadRequestException("Cannot delete book with active borrowings");
        }

        book.setArchived(true);
        bookRepository.save(book);

        auditLogRepository.save(
                AuditLog.builder()
                        .action("DELETE_BOOK")
                        .performedBy(profile)
                        .timestamp(LocalDateTime.now())
                        .details("Book ID: " + bookId)
                        .build()
        );

        logger.info("Archived book ID {}", bookId);
    }

    public Page<BookResponse> getAllBooks(
            Long id,
            String title,
            String author,
            String shelfLocation,
            LocalDateTime createdAt,
            int pageNo,
            int pageSize,
            String sortBy,
            String sortDirection) {
        String email = getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));

        School school = admin.getSchool();
        if (school == null) {
            throw new CustomInternalServerException("Admin is not associated with any school");
        }

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable paging = PageRequest.of(pageNo, pageSize, sort);

        Page<Book> booksPage = bookRepository.findAllBySchoolWithFilters(
                school.getId(),
                id,
                title,
                author,
                shelfLocation,
                createdAt,
                paging);

        return booksPage.map(this::mapToBookResponse);
    }

    private BookResponse mapToBookResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .rackNo(book.getShelfLocation())
                .quantityAvailable(book.getQuantityAvailable())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }

    @Transactional
    @Override
    public BookBorrowing borrowBook( Long bookId, LocalDateTime dueDate) {;
        String email = SecurityConfig.getAuthenticatedUserEmail();

        Profile profile = profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("Member profile not found"));
      if(profile.getUser().getSchool().getSupportsLibraryMembership()) {
           LibraryMembership membership = libraryMemberRepository.findByStudentAndStatus(profile, MembershipStatus.ACTIVE)
            .orElseThrow(() -> new BadRequestException("Student does not have an active library membership"));
        }
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomNotFoundException("Book not found"));

        if (!book.getSchool().getId().equals(profile.getUser().getSchool().getId())) {
            throw new NotFoundException("Book not available in your school");
        }

        long activeBorrowings = bookBorrowingRepository.countByStudentProfileAndStatusNot(profile, BorrowingStatus.RETURNED);
        if (activeBorrowings >= 3) {
            throw new BadRequestException("Cannot borrow more than 3 books at a time");
        }

        BigDecimal unpaidFines = bookBorrowingRepository.sumUnpaidFinesByProfile(profile);
        if (unpaidFines != null && unpaidFines.compareTo(BigDecimal.ZERO) > 0) {
            throw new BadRequestException("Please clear outstanding fines before borrowing");
        }

        BookBorrowing existingBorrowing = bookBorrowingRepository.findByStudentProfileAndBookAndStatusNot(
                profile, book, BorrowingStatus.RETURNED);
        if (existingBorrowing != null) {
            throw new EntityAlreadyExistException("You have already borrowed this book");
        }

        if (book.getQuantityAvailable() <= 0) {
            throw new BadRequestException("No available copies of the book");
        }

        if (dueDate == null) {
            dueDate = LocalDateTime.now().plusWeeks(2);
        }
        if (dueDate.isBefore(LocalDateTime.now()) || dueDate.isAfter(LocalDateTime.now().plusMonths(1))) {
            throw new BadRequestException("Due date must be within 1 month from now");
        }

        book.setQuantityAvailable(book.getQuantityAvailable() - 1);
        bookRepository.save(book);

        BookBorrowing borrowing = BookBorrowing.builder()
                .studentProfile(profile)
                .book(book)
                .borrowDate(LocalDateTime.now())
                .dueDate(dueDate)
                .status(BorrowingStatus.BORROWED)
                .fineAmount(BigDecimal.ZERO)
                .build();

        logger.info("Borrowing book {} ", bookId);
        return bookBorrowingRepository.save(borrowing);
    }

    @Transactional
    @Override
    public BookBorrowing returnBook(Long borrowingId) {
        String email = SecurityConfig.getAuthenticatedUserEmail();

        Profile profile = profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("Member profile not found"));

        BookBorrowing borrowing = bookBorrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new CustomNotFoundException("Borrowing record not found"));

        if (borrowing.getStatus() != BorrowingStatus.BORROWED) {
            throw new BadRequestException("Book is not currently borrowed");
        }

        Book book = borrowing.getBook();
        book.setQuantityAvailable(book.getQuantityAvailable() + 1);
        bookRepository.save(book);

        LocalDateTime returnDate = LocalDateTime.now();
        borrowing.setActualReturnDate(returnDate);
        borrowing.setStatus(BorrowingStatus.RETURNED);

        if (returnDate.isAfter(borrowing.getDueDate())) {
            borrowing.setLate(true);
            long daysLate = ChronoUnit.DAYS.between(borrowing.getDueDate(), returnDate);

            BigDecimal lateFee = profile.getUser().getSchool().getLibraryBookLateReturnFee();

            if (lateFee != null) {
                BigDecimal fee = BigDecimal.valueOf(daysLate).multiply(lateFee);
                borrowing.setFineAmount(fee);
            } else {
                borrowing.setFineAmount(BigDecimal.ZERO);
            }
        }

        logger.info("Returning book for borrowing ID {}", borrowingId);
        BookBorrowing savedBorrowing = bookBorrowingRepository.save(borrowing);

        notifyNextReservation(book);

        return savedBorrowing;
    }

    @Transactional
    public BookReservation reserveBook(Long memberId, Long bookId) {
        Profile profile = profileRepository.findByUserId(memberId)
                .orElseThrow(() -> new CustomNotFoundException("Member profile not found"));

        LibraryMembership membership = libraryMemberRepository.findByStudentAndStatus(profile, MembershipStatus.ACTIVE)
                .orElseThrow(() -> new BadRequestException("Student does not have an active library membership"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomNotFoundException("Book not found"));

        if (book.getQuantityAvailable() > 0) {
            throw new BadRequestException("Book is currently available for borrowing");
        }

        if (bookReservationRepository.existsByStudentProfileAndBookAndStatus(
                profile, book, ReservationStatus.PENDING)) {
            throw new EntityAlreadyExistException("You have already reserved this book");
        }

        BookReservation reservation = BookReservation.builder()
                .studentProfile(profile)
                .book(book)
                .reservationDate(LocalDateTime.now())
                .status(ReservationStatus.PENDING)
                .build();

        logger.info("Reserving book {} for member {}", bookId, memberId);
        return bookReservationRepository.save(reservation);
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * ?") // Daily at midnight
    public void updateLateStatuses() {
        List<BookBorrowing> borrowings = bookBorrowingRepository.findByStatusAndDueDateBefore(
                BorrowingStatus.BORROWED, LocalDateTime.now());
        borrowings.forEach(borrowing -> {
            borrowing.setLate(true);
            bookBorrowingRepository.save(borrowing);
        });
        logger.info("Updated late statuses for {} borrowings", borrowings.size());
    }

    public List<BookBorrowing> getStudentBorrowingHistory(Long memberId) {
        Profile profile = profileRepository.findByUserId(memberId)
                .orElseThrow(() -> new CustomNotFoundException("Member profile not found"));
        return bookBorrowingRepository.findByStudentProfile(profile);
    }

    public List<BookBorrowing> getBookBorrowingHistory(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomNotFoundException("Book not found"));
        return bookBorrowingRepository.findByBook(book);
    }

    private void notifyNextReservation(Book book) {
        BookReservation reservation = bookReservationRepository.findFirstByBookAndStatusOrderByReservationDateAsc(
                book, ReservationStatus.PENDING);
        if (reservation != null) {
            // Placeholder for notification logic (e.g., email or in-app)
            logger.info("Notifying user {} for reserved book {}",
                    reservation.getStudentProfile().getUser().getEmail(), book.getTitle());
            // reservation.setStatus(ReservationStatus.FULFILLED); // Optional: Mark as fulfilled
            // bookReservationRepository.save(reservation);
        }
    }

    private String getAuthenticatedUserEmail() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            throw new CustomNotFoundException("Unable to authenticate user");
        }
    }
}
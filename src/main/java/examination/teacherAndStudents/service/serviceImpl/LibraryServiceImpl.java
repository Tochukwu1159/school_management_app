package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.BookRequest;
import examination.teacherAndStudents.dto.BookResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.*;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.LibraryService;
import examination.teacherAndStudents.utils.BorrowingStatus;
import examination.teacherAndStudents.utils.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LibraryServiceImpl implements LibraryService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookBorrowingRepository bookBorrowingRepository;


    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProfileRepository profileRepository;

    @Override
    public Book addBook(BookRequest book) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN)
                    .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));
            Optional<User> userDetails = userRepository.findByEmail(email);
            Book newBook = new Book();
            // Set the initial quantity available to the total quantity
            newBook.setQuantityAvailable(book.getQuantityAvailable());
            newBook.setRackNo(book.getRackNo());
            newBook.setAuthor(book.getAuthor());
            newBook.setSchool(userDetails.get().getSchool());
            newBook.setTitle(book.getTitle());
            return bookRepository.save(newBook);
        } catch (Exception e) {
            throw new CustomInternalServerException("Error adding book: " + e.getMessage());
        }
    }
    @Override
    public Book updateBookQuantity(Long bookId, int quantityToAdd) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN)
                    .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));
            Optional<Book> optionalBook = bookRepository.findById(bookId);

            if (optionalBook.isPresent()) {
                Book book = optionalBook.get();

                // Increase the quantity by the specified amount
                int currentQuantity = book.getQuantityAvailable();
                book.setQuantityAvailable(currentQuantity + quantityToAdd);

                // Save the updated book
                return bookRepository.save(book);
            } else {
                throw new CustomInternalServerException("Book not found with ID: " + bookId);
            }
        } catch (Exception e) {
            throw new CustomInternalServerException("Error updating book: " + e.getMessage());

        }
    }

    @Override
    public Book editBook(Long bookId, BookRequest updatedBook) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN)
                    .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));
            Book existingBook = bookRepository.findById(bookId)
                    .orElseThrow(() -> new CustomInternalServerException("Book not found"));

            // Update fields based on your requirements
            existingBook.setTitle(updatedBook.getTitle());
            existingBook.setAuthor(updatedBook.getAuthor());
            existingBook.setQuantityAvailable(updatedBook.getQuantityAvailable());

            return bookRepository.save(existingBook);
        } catch (Exception e) {
            throw new CustomInternalServerException("Error editing book: " + e.getMessage());
        }
    }

    @Override
    public void deleteBook(Long bookId) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN)
                    .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));
            bookRepository.deleteById(bookId);
        } catch (Exception e) {
            throw new CustomInternalServerException("Error deleting book: " + e.getMessage());
        }
    }


    public Page<BookResponse> getAllBooks(
            Long id,
            String title,
            String author,
            LocalDateTime createdAt,
            int pageNo,
            int pageSize,
            String sortBy,
            String sortDirection) {

        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN)
                    .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));

            // Create Pageable object with sorting
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            Pageable paging = PageRequest.of(pageNo, pageSize, sort);

            // Fetch filtered books
            Page<Book> booksPage = bookRepository.findAllBySchoolWithFilters(
                    admin.getSchool().getId(),
                    id,
                    title,
                    author,
                    createdAt,
                    paging);

            // Map to response DTO
            return booksPage.map(this::mapToBookResponse);
        } catch (CustomNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomInternalServerException("Error fetching books: " + e.getMessage());
        }
    }

    private BookResponse mapToBookResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .rackNo(book.getRackNo())
                .quantityAvailable(book.getQuantityAvailable())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }

    @Transactional
    @Override
    public BookBorrowing borrowBook(Long memberId, Long bookId, LocalDateTime dueDate) {
        try {
            Profile profile = profileRepository.findByUserId(memberId)
                    .orElseThrow(() -> new CustomNotFoundException("Member profile not found"));

            // Validate book
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new CustomNotFoundException("Book not found"));

            // Check school consistency
            if (!book.getSchool().getId().equals(profile.getUser().getSchool().getId())) {
                throw new NotFoundException("Book not available in your school");
            }

            // Check if already borrowed
            BookBorrowing existingBorrowing = bookBorrowingRepository.findByStudentProfileAndBookAndStatusNot(
                    profile, book, BorrowingStatus.RETURNED);
            if (existingBorrowing != null) {
                throw new EntityAlreadyExistException("You have already borrowed this book");
            }

            // Check availability
            if (book.getQuantityAvailable() <= 0) {
                throw new BadRequestException("No available copies of the book");
            }

            // Update book quantity
            book.setQuantityAvailable(book.getQuantityAvailable() - 1);
            bookRepository.save(book);

            // Create borrowing record
            BookBorrowing borrowing = BookBorrowing.builder()
                    .studentProfile(profile)
                    .book(book)
                    .borrowDate(LocalDateTime.now())
                    .dueDate(dueDate) // 2 weeks loan period
                    .status(BorrowingStatus.BORROWED)
                    .build();

            return bookBorrowingRepository.save(borrowing);
        } catch (CustomNotFoundException  |
                 EntityAlreadyExistException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomInternalServerException("Error borrowing book: " + e.getMessage());
        }
    }


    @Transactional
    @Override
    public BookBorrowing returnBook(Long borrowingId) {
        try {
            // Validate borrowing record
            BookBorrowing borrowing = bookBorrowingRepository.findById(borrowingId)
                    .orElseThrow(() -> new CustomNotFoundException("Borrowing record not found"));

            // Check current status
            if (borrowing.getStatus() != BorrowingStatus.BORROWED) {
                throw new BadRequestException("Book is not currently borrowed");
            }

            // Get associated book
            Book book = borrowing.getBook();

            // Update book quantity
            book.setQuantityAvailable(book.getQuantityAvailable() + 1);
            bookRepository.save(book);

            // Update borrowing record
            LocalDateTime returnDate = LocalDateTime.now();
            borrowing.setActualReturnDate(returnDate);
            borrowing.setStatus(BorrowingStatus.RETURNED);

            // Check for late return
            if (returnDate.isAfter(borrowing.getDueDate())) {
                borrowing.setLate(true);
                // Could add late fee calculation here
            }

            return bookBorrowingRepository.save(borrowing);
        } catch (CustomNotFoundException | BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomInternalServerException("Error returning book: " + e.getMessage());
        }
    }
}


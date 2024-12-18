package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.BookRequest;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.LibraryService;
import examination.teacherAndStudents.utils.BorrowingStatus;
import examination.teacherAndStudents.utils.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

    private final LibraryMemberRepository libraryMemberRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProfileRepository profileRepository;

    @Override
    public Book addBook(BookRequest book) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
            if (admin == null) {
                throw new CustomNotFoundException("Please login as an Admin");
            }
            Book newBook = new Book();
            // Set the initial quantity available to the total quantity
            newBook.setQuantityAvailable(book.getQuantityAvailable());
            newBook.setRackNo(book.getRackNo());
            newBook.setAuthor(book.getAuthor());
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
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
            if (admin == null) {
                throw new CustomNotFoundException("Please login as an Admin"); // Return unauthorized response for non-admin users
            }
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
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
            if (admin == null) {
                throw new CustomNotFoundException("Please login as an Admin"); // Return unauthorized response for non-admin users
            }
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
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
            if (admin == null) {
                throw new CustomNotFoundException("Please login as an Admin");
            }
            bookRepository.deleteById(bookId);
        } catch (Exception e) {
            throw new CustomInternalServerException("Error deleting book: " + e.getMessage());
        }
    }


    @Override
    public Page<Book> getAllBooks(int pageNo, int pageSize, String sortBy)  {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
        if (admin == null) {
            throw new CustomNotFoundException("Please login as an Admin");
        }
        try {
            Pageable paging = PageRequest.of(pageNo, pageSize);
            return bookRepository.findAll(paging);
        } catch (Exception e) {
            throw new CustomInternalServerException("Error fetching all books: " + e.getMessage());
        }
    }

    @Override
    public BookBorrowing borrowBook(Long memberId, Long bookId) {

        try {
            User user = userRepository.findById(memberId)
                    .orElseThrow(() -> new CustomInternalServerException("User not found"));
            Profile profile = profileRepository.findByUser(user)
                    .orElseThrow(() -> new CustomInternalServerException("User profile not found"));

            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new CustomInternalServerException("Book not found"));

            // Check if the student has already borrowed the book with a status other than RETURNED
            BookBorrowing existingBorrowing = bookBorrowingRepository.findByStudentProfileAndBookAndStatusNot(
                   profile, book, BorrowingStatus.RETURNED);

            if (existingBorrowing != null) {
                throw new CustomInternalServerException("You have already borrowed this book.");
            }

            // Check if there are available copies
            if (book.getQuantityAvailable() > 0) {
                // Decrement the quantity available
                book.setQuantityAvailable(book.getQuantityAvailable() - 1);

                // Save the updated book
                bookRepository.save(book);

                // Create a borrowing entry
                BookBorrowing borrowing = new BookBorrowing();
                borrowing.setStudentProfile(profile);
                borrowing.setBook(book);
                borrowing.setBorrowDate(LocalDateTime.now());
                borrowing.setStatus(BorrowingStatus.BORROWED);

                return bookBorrowingRepository.save(borrowing);
            } else {
                throw new CustomInternalServerException("No available copies of the book");
            }
        } catch (Exception e) {
            throw new CustomInternalServerException("Error borrowing book: " + e.getMessage());
        }
    }



    @Override
    public BookBorrowing returnBook(Long borrowingId) {
        try {
            BookBorrowing borrowing = bookBorrowingRepository.findById(borrowingId)
                    .orElseThrow(() -> new CustomInternalServerException("Borrowing entry not found"));

            // Check if the book is already returned
            if (borrowing.getStatus() == BorrowingStatus.RETURNED) {
                throw new CustomInternalServerException("Book already returned");
            }

            // Increment the quantity available
            Book book = borrowing.getBook();
            book.setQuantityAvailable(book.getQuantityAvailable() + 1);

            // Save the updated book
            bookRepository.save(book);

            // Update the borrowing entry
            borrowing.setReturnDate(LocalDateTime.now());
            borrowing.setStatus(BorrowingStatus.RETURNED);

            return bookBorrowingRepository.save(borrowing);
        } catch (Exception e) {
            throw new CustomInternalServerException("Error returning book: " + e.getMessage());
        }
    }
}



package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.BookAssignmentRequest;
import examination.teacherAndStudents.dto.BookSaleRequest;
import examination.teacherAndStudents.dto.BookSaleResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.AuthenticationFailedException;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.BookSaleService;
import examination.teacherAndStudents.service.PaymentService;
import examination.teacherAndStudents.service.TransactionService;
import examination.teacherAndStudents.utils.Roles;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookSaleServiceImpl implements BookSaleService {

    private final BookSaleRepository bookSaleRepository;
    private final ClassLevelRepository classLevelRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;


    public List<BookSaleResponse> getAllBooks() {
        return bookSaleRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    public BookSaleResponse getBookById(Long id) {
        BookSale bookSale = bookSaleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book not found"));
        return convertToResponse(bookSale);
    }

    @Transactional
    @Override
    public BookSaleResponse createBookSale(BookSaleRequest request) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
        if (admin == null) {
            throw new AuthenticationFailedException("Please login as an Admin");
        }

        Optional<User> userDetails = userRepository.findByEmail(email);
        ClassLevel classLevel = classLevelRepository.findById(request.getClassId())
                .orElseThrow(() -> new RuntimeException("Class level not found"));

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        BookSale bookSale = BookSale.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .idNo(request.getIdNo())
                .inStock(true)
                .numberOfCopies(request.getNumberOfCopies())
                .price(request.getPrice())
                .classLevel(classLevel)
                .subject(subject)
                .school(userDetails.get().getSchool())
                .build();

        bookSale = bookSaleRepository.save(bookSale);

        return convertToResponse(bookSale);
    }

    private BookSaleResponse convertToResponse(BookSale bookSale) {
        return BookSaleResponse.builder()
                .id(bookSale.getId())
                .title(bookSale.getTitle())
                .author(bookSale.getAuthor())
                .idNo(bookSale.getIdNo())
                .price(bookSale.getPrice())
                .numberOfCopies(bookSale.getNumberOfCopies())
                .classLevelName(bookSale.getClassLevel().getClassName())
                .subjectName(bookSale.getSubject().getName())
                .build();
    }
}

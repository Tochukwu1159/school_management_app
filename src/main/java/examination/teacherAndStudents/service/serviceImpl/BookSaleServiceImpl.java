
package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.BookAssignmentRequest;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.BookSaleService;
import examination.teacherAndStudents.service.PaymentService;
import examination.teacherAndStudents.service.TransactionService;
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
    private final BookSaleAllocationRepository bookSaleTrackerRepository;
    private final PaymentService paymentService;
    private final TransactionService transactionService;
    private final AcademicSessionRepository academicSessionRepository;
    private final TransactionRepository transactionRepository;

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

}

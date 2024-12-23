package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.BookAssignmentRequest;
import examination.teacherAndStudents.dto.BookSaleRequest;
import examination.teacherAndStudents.dto.BookSaleResponse;
import examination.teacherAndStudents.entity.BookSale;

import java.util.List;

public interface BookSaleService {
    BookSaleResponse getBookById(Long id);
    List<BookSaleResponse> getAllBooks();
    BookSaleResponse createBookSale(BookSaleRequest request);}

package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.entity.BookSale;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Setter
@Getter
@Builder
public class BookPaymentResponse {
    // Getters and Setters
    private String paymentId;
    private double totalAmountPaid;
    private List<BookSale> books;
    private String paymentStatus;

    // Constructor
    public BookPaymentResponse(String paymentId, double totalAmountPaid, List<BookSale> books, String paymentStatus) {
        this.paymentId = paymentId;
        this.totalAmountPaid = totalAmountPaid;
        this.books = books;
        this.paymentStatus = paymentStatus;
    }

}

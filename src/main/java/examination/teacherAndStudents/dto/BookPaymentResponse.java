package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.entity.BookSale;
import lombok.Builder;

import java.util.List;
@Builder
public class BookPaymentResponse {
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

    // Getters and Setters
    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public double getTotalAmountPaid() {
        return totalAmountPaid;
    }

    public void setTotalAmountPaid(double totalAmountPaid) {
        this.totalAmountPaid = totalAmountPaid;
    }

    public List<BookSale> getBooks() {
        return books;
    }

    public void setBooks(List<BookSale> books) {
        this.books = books;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}

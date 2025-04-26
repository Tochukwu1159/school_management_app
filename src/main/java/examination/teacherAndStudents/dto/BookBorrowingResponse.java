package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.BorrowingStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class BookBorrowingResponse {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private Long profileId;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private LocalDateTime actualReturnDate;
    private boolean late;
    private BorrowingStatus status;
    private BigDecimal fineAmount;
}
package examination.teacherAndStudents.entity;

import examination.teacherAndStudents.utils.BorrowingStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "book_borrowing")
public class BookBorrowing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile studentProfile;

    @NotNull
    private LocalDateTime borrowDate;

    @NotNull
    private LocalDateTime dueDate;

    private boolean late;

    private LocalDateTime actualReturnDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BorrowingStatus status;

    @Column(nullable = false, columnDefinition = "DECIMAL(10,2)")
    private BigDecimal fineAmount;

    @PrePersist
    protected void onCreate() {
        if (borrowDate == null) {
            borrowDate = LocalDateTime.now();
        }
        if (dueDate == null) {
            dueDate = borrowDate.plusWeeks(2);
        }
        if (fineAmount == null) {
            fineAmount = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (status == BorrowingStatus.RETURNED && actualReturnDate == null) {
            actualReturnDate = LocalDateTime.now();
        }
    }
}
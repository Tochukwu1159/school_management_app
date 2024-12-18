package examination.teacherAndStudents.entity;

import examination.teacherAndStudents.utils.BorrowingStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
//@Table(name = "book_borrowing", indexes = {
//        @Index(name = "idx_book_borrowing_book_id", columnList = "book_id"),
//        @Index(name = "idx_book_borrowing_user_id", columnList = "user_id"),
//        @Index(name = "idx_book_borrowing_status", columnList = "status")
//})
@Entity
@Builder
@Table(name = "book_borrowing")
public class BookBorrowing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;  // The book being borrowed

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile studentProfile;  // The student borrowing the book (assuming User represents student)

    @NotNull
    private LocalDateTime borrowDate;  // Date and time when the book was borrowed

    private LocalDateTime returnDate;  // Date and time when the book is expected/was returned

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BorrowingStatus status;  // The status of the borrowing (e.g., borrowed, returned)

    @PrePersist
    protected void onCreate() {
        if (borrowDate == null) {
            borrowDate = LocalDateTime.now();  // Set the borrowDate to current time if not provided
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (status == BorrowingStatus.RETURNED && returnDate == null) {
            returnDate = LocalDateTime.now();  // Set returnDate when the status is 'RETURNED'
        }
    }
}

package examination.teacherAndStudents.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "book_tracker")
public class BookTracker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private BookSale bookSale;

    @ManyToOne
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicSession academicYear;

    @Column(name = "book_remaining", nullable = false)
    private int bookRemaining;

    @PrePersist
    protected void onCreate() {
        this.bookRemaining = 0;
    }

}

package examination.teacherAndStudents.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entity representing a fee, associated with a fee category.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "fee")
@Entity
@Builder
public class Fee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private FeeCategory category;

    private String description;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private boolean isCompulsory;

    @ManyToOne
    @JoinColumn(nullable = false)
    private School school;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean archived = false;

    @ManyToOne
    @JoinColumn(nullable = false)
    private AcademicSession session;

    @ManyToOne
    private ClassLevel classLevel; // Optional

    @ManyToOne
    private ClassBlock subClass; // Optional

    @ManyToOne
    private StudentTerm term; // Optional

    public boolean isApplicableFor(ClassLevel classLevel, ClassBlock subClass, StudentTerm term) {
        return (this.classLevel == null || this.classLevel.equals(classLevel)) &&
                (this.subClass == null || this.subClass.equals(subClass)) &&
                (this.term == null || this.term.equals(term));
    }

    public boolean isCurrentlyActive() {
        return active && !archived;
    }
}
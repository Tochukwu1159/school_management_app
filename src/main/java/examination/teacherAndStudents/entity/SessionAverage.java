package examination.teacherAndStudents.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "session_average")
public class SessionAverage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile userProfile;

    @ManyToOne
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicSession academicYear;

    @ManyToOne
    @JoinColumn(name = "classblock_id", nullable = false)
    private ClassBlock classBlock;

    @Column(nullable = false)
    private double averageScore;  // The average score of the three terms

    @ManyToOne
    @JoinColumn(name = "first_term_position_id", nullable = false)
    private Position firstTermPosition;

    @ManyToOne
    @JoinColumn(name = "second_term_position_id", nullable = false)
    private Position secondTermPosition;

    @ManyToOne
    @JoinColumn(name = "third_term_position_id", nullable = false)
    private Position thirdTermPosition;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

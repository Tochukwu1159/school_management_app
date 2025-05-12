package examination.teacherAndStudents.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import examination.teacherAndStudents.utils.SessionPromotion;
import examination.teacherAndStudents.utils.SessionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "academic_year")
@Entity
public class AcademicSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_name_id")

    private SessionName sessionName;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    private LocalDate resultReadyDate;

    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
    private SessionPromotion sessionPromotion = SessionPromotion.PENDING;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private
    SessionStatus status = SessionStatus.ACTIVE; // Default to ACTIVE when created


    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @OneToMany(mappedBy = "academicSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentTerm> studentTerms = new ArrayList<>();


    // Method to validate and update status
    @PrePersist
    public void validateAndSetStatus() {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        // Default to OPEN if not set
        if (this.status == null) {
            this.status = SessionStatus.OPEN;
        }

        // Automatically set to CLOSED if end date passed
        if (endDate.isBefore(LocalDate.now())) {
            this.status =
                    SessionStatus.CLOSED;
        }

        // Additional business logic as needed

    }
}

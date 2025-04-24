package examination.teacherAndStudents.entity;

import examination.teacherAndStudents.utils.DisciplinaryActionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "disciplinary_actions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisciplinaryAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @Enumerated(EnumType.STRING)
    private DisciplinaryActionType actionType;

    private String reason;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    @Column(name = "is_active")
    private boolean active;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "issued_by")
    private Profile issuedBy; // Staff member who issued the action

    @ManyToOne
    @JoinColumn(name = "school_id")
    private School school;

    public boolean isCurrentlyActive() {
        return active && LocalDate.now().isAfter(startDate) &&
                (endDate == null || LocalDate.now().isBefore(endDate));
    }
}

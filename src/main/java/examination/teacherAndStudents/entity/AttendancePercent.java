package examination.teacherAndStudents.entity;
import examination.teacherAndStudents.utils.AttendanceStatus;
import jakarta.persistence.*;
        import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
//@Table(name = "attendance_percent", indexes = {
//        @Index(name = "idx_user_class_year", columnList = "user_id, classlevel_id, academic_year_id")
//})
@Entity
@Builder
@Table(name = "attendance_percent")
public class AttendancePercent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Min(0)
    private Double attendancePercentage;

    @ManyToOne
    @JoinColumn(name = "term_id", nullable = false)
    private StudentTerm studentTerm;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Profile user;

    @ManyToOne
    @JoinColumn(name = "classblock_id", nullable = false)
    private ClassBlock classBlock;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicSession academicYear;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    private void setLastUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

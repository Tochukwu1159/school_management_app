package examination.teacherAndStudents.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
//@Table(name = "position", indexes = {
//        @Index(name = "idx_position_user", columnList = "user_id"),
//        @Index(name = "idx_position_classLevel", columnList = "classLevel_id"),
//        @Index(name = "idx_position_academicYear", columnList = "academic_year_id")
//})
@Entity
@Builder
@Table(name = "position")
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private double averageScore;

    @ManyToOne
    @JoinColumn(name = "profile_id")
    @JsonBackReference
    private Profile userProfile;

    @Column(name = "position_rank", nullable = false)
    private int positionRank;

    @ManyToOne
    @JoinColumn(name = "session_class_id", nullable = false)
    private SessionClass sessionClass;

    @ManyToOne
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicSession academicYear;

    @ManyToOne
    @JoinColumn(name = "term_id", nullable = false)
    private StudentTerm studentTerm;


    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}

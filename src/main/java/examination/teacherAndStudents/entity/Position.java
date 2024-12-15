package examination.teacherAndStudents.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import examination.teacherAndStudents.utils.StudentTerm;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Data
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
    @JoinColumn(name = "classblock_id", nullable = false)
    private ClassBlock classBlock;

    @OneToOne
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicSession academicYear;

    @Enumerated(EnumType.STRING)
    private StudentTerm term;


    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}

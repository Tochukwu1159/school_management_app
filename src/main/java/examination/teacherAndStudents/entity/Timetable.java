package examination.teacherAndStudents.entity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import examination.teacherAndStudents.utils.DayOfWeek;
import examination.teacherAndStudents.utils.TimetableType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "timetable")
@Entity
@Builder
public class Timetable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classBlock_id")
    private ClassBlock classBlock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TimetableType timetableType;

    @ManyToOne
    @JoinColumn(name = "term_id", nullable = false)
    private StudentTerm term;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicSession academicYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "created_at", nullable = false, updatable = false) // Track the creation date
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = new java.util.Date();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = new java.util.Date();
    }

    @OneToMany(mappedBy = "timetable", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubjectSchedule> subjectSchedules;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;


    // Getters and setters
}

package examination.teacherAndStudents.entity;

import examination.teacherAndStudents.utils.TeachingStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "subject_schedule")
@Entity
@Builder
public class SubjectSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "timetable_id", nullable = false)
    private Timetable timetable;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private ClassSubject subject; // Nullable for breaks

    @NotNull(message = "Start time must not be null")
    private String startTime;

    @NotNull(message = "End time must not be null")
    private String endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private Profile teacher; // Nullable for breaks

    @Column(nullable = false)
    private boolean isBreak = false; // Flag to indicate if this is a break period
}
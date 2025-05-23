package examination.teacherAndStudents.entity;

import examination.teacherAndStudents.utils.AttendanceStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "staff_attendance")
public class StaffAttendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    @NotNull(message = "Teacher must not be null")
        private Profile staff;

    @Column(name = "attendance_date")
    @NotNull(message = "Attendance date must not be null") // Ensure date is not null
    @PastOrPresent(message = "Attendance date must be in the past or present")
    private LocalDateTime date;


    @Enumerated(EnumType.STRING)
    @NotNull(message = "Attendance status must not be null")
    private AttendanceStatus status; // Enum for present or absent

    @ManyToOne
    @JoinColumn(name = "term_id", nullable = false)
    private StudentTerm studentTerm;

    @ManyToOne
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicSession academicYear;

    // Constructors, getters, and setters

}


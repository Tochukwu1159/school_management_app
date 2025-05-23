package examination.teacherAndStudents.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Year;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "staff_attendance_percent")
@Entity
@Builder
public class StaffAttendancePercent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attendance_percentage", nullable = false)
    private Double attendancePercentage;

    @Column(name = "days_present")
    private Long daysPresent;

    @Column(name = "days_absent")
    private Long daysAbsent;

    @Column(name = "days_late")
    private Long daysLate;

    @Column(name = "total_days")
    private Long totalDays;

    @ManyToOne
    @JoinColumn(name = "term_id", nullable = false)
    private StudentTerm studentTerm;

    @ManyToOne
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicSession academicYear;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Profile staff;
}
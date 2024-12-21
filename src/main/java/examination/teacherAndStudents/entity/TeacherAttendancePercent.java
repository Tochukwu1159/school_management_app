package examination.teacherAndStudents.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Table(name = "teacher_attendance_percent")
    @Entity
    @Builder
    public class TeacherAttendancePercent {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false)
        @NotNull(message = "Attendance percentage must not be null")
        @Min(value = 0, message = "Attendance percentage must be at least 0")
        @Max(value = 100, message = "Attendance percentage must be at most 100")
        private Double attendancePercentage;

        @ManyToOne
        @JoinColumn(name = "teacher_id", nullable = false)
        private Profile teacher;

        @ManyToOne
        @JoinColumn(name = "term_id", nullable = false)
        private StudentTerm studentTerm;


        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "academic_year_id", nullable = false)
        private AcademicSession academicYear;


    }


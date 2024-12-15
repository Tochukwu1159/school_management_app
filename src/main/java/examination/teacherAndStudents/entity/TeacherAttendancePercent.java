package examination.teacherAndStudents.entity;

import examination.teacherAndStudents.utils.StudentTerm;
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

        @OneToOne
        @JoinColumn(name = "attendance_id", nullable = false)
        private TeacherAttendance attendance;

        @ManyToOne
        @JoinColumn(name = "teacher_id", nullable = false)
        private Profile teacher;
        @Enumerated(EnumType.STRING)
        private StudentTerm studentTerm; // Enum for present or absent


    }


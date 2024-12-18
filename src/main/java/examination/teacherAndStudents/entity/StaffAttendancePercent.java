package examination.teacherAndStudents.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Year;

@Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Table(name = "staff_attendance_percent")
    @Entity
    @Builder
    public class StaffAttendancePercent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

        private Double attendancePercentage;

    @ManyToOne
    @JoinColumn(name = "term_id", nullable = false)
    private StudentTerm studentTerm;

        private Year year;


        @ManyToOne
        @JoinColumn(name = "user_id", nullable = false)
        private User user;


    }


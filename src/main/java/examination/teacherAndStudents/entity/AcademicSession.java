package examination.teacherAndStudents.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.Year;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "academic_year")
@Entity
@Builder
public class AcademicSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;

}


//{
//        "name": "2024/2025 Academic Year",
//        "startDate": "2024-09-01",
//        "endDate": "2025-06-30"
//        }

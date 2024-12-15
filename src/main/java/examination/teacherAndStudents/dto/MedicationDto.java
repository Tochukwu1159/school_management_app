package examination.teacherAndStudents.dto;

import jakarta.persistence.Column;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MedicationDto {


        @Column(nullable = false)
        private LocalDate recordDate;

        @Column(columnDefinition = "TEXT")
        private String details;
}

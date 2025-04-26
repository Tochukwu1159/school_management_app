package examination.teacherAndStudents.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class EmergencyContact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String phone;
    private String relationship;


}

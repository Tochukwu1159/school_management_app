package examination.teacherAndStudents.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "services_offered")
public class ServiceOffered {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    @ManyToMany(mappedBy = "selectedServices")
    private List<School> schools;
}
package examination.teacherAndStudents.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "biometric_templates")
public class BiometricTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "staff_id", nullable = false)
    private Profile staff;

    @Lob
    @Column(nullable = false)
    private byte[] templateData;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    // ... getters and setters ...
}
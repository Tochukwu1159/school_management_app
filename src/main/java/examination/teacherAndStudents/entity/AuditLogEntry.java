package examination.teacherAndStudents.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name = "audit_log")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String entityType;

    @Column(nullable = false)
    private String entityId;

    private String oldValue;
    private String newValue;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private BigDecimal amount; // For financial transactions
}

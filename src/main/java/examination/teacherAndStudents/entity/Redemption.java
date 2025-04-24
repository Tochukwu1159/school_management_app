package examination.teacherAndStudents.entity;

import examination.teacherAndStudents.utils.RedemptionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "redemption")
@Entity
@Builder
public class Redemption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "profile_id")
    private Profile user;

    @ManyToOne
    @JoinColumn(name = "school_id")
    private School school;

    private Integer pointsRedeemed;
    private BigDecimal amount;
    private LocalDateTime redemptionDate;

    @Enumerated(EnumType.STRING)
    private RedemptionStatus status;
}
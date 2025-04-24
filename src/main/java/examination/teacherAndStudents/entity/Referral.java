package examination.teacherAndStudents.entity;
import examination.teacherAndStudents.utils.ReferralStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "redemption")
@Entity
@Builder
public class Referral {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "referring_profile_id")
    private Profile referringUser;

    @ManyToOne
    @JoinColumn(name = "referred_profile_id")
    private Profile referredUser;

    @ManyToOne
    @JoinColumn(name = "school_id")
    private School school;

    private LocalDateTime referralDate;

    @Enumerated(EnumType.STRING)
    private ReferralStatus status;


}

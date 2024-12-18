package examination.teacherAndStudents.entity;

import examination.teacherAndStudents.utils.VisitorStatus;
import examination.teacherAndStudents.utils.VisitorType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "visitors")
public class Visitors {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;


    @Column(name = "name")
    private String name;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "visitor_type")
    private VisitorType visitorType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private VisitorStatus status;

    @NotBlank(message = "Purpose is required")
    @Size(max = 255, message = "Purpose must be less than 255 characters")
    @Column(name = "purpose")
    private String purpose;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Profile host;

    @CreationTimestamp
    @Column(name = "signIn", updatable = false, nullable = false)
    private LocalDateTime signIn;

    @UpdateTimestamp
    @Column(name = "signOut")
    private LocalDateTime signOut;

    public void checkOut() {
        if (this.status == VisitorStatus.CHECKED_OUT) {
            this.signOut = LocalDateTime.now();
        }
    }

}

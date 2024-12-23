package examination.teacherAndStudents.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "school")
public class School {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String schoolName;

    @Column(nullable = false)
    private String schoolAddress;

    private String schoolLogoUrl;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(unique = true, nullable = false)
    private String subscriptionKey;

    @Column(nullable = false)
    private LocalDate subscriptionExpiryDate;

    @Column(nullable = false)
    private Boolean isActive;

    @ElementCollection
    private List<String> selectedServices;

//    @OneToMany(mappedBy = "school", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<AcademicSession> academicSessions;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public boolean isSubscriptionValid() {
        return subscriptionExpiryDate.isAfter(LocalDate.now());
    }
}

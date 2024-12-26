package examination.teacherAndStudents.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import examination.teacherAndStudents.utils.ServiceType;
import examination.teacherAndStudents.utils.SubscriptionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_type")
    private SubscriptionType subscriptionType;

    @Column(unique = true, nullable = false)
    private String subscriptionKey;

    private LocalDateTime subscriptionExpiryDate;


    @Column(nullable = false)
    private Boolean isActive;


    @ManyToMany
    @JoinTable(
            name = "school_services",
            joinColumns = @JoinColumn(name = "school_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    private List<ServiceOffered> selectedServices = new ArrayList<>();


    @JsonManagedReference
    @OneToMany(mappedBy = "school", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User> users;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isSubscriptionValid() {
        return subscriptionExpiryDate.isAfter(LocalDateTime.now());
    }
}

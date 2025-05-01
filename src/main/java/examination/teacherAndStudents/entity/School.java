package examination.teacherAndStudents.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import examination.teacherAndStudents.utils.ServiceType;
import examination.teacherAndStudents.utils.SubscriptionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String

            schoolMotto;

    @Column(nullable = false)
    private Integer establishedYear;

    @Column(nullable = false, unique = true)
    private String schoolIdentificationNumber;

    @Column(unique = true)
    private String alternatePhoneNumber;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_type")
    private SubscriptionType subscriptionType;

    @Column(unique = true)
    private String subscriptionKey;

    private String schoolCode;

    private String schoolPrimaryColour;

    @Column(name = "is_application_fee", nullable = false, columnDefinition = "boolean default false")
    private Boolean isApplicationFee = false;

    @Column(name = "supports_library_membership", nullable = false, columnDefinition = "boolean default false")
    private Boolean supportsLibraryMembership = false;

    @Column(name = "supports_late_book_penalty_fee", nullable = false, columnDefinition = "boolean default false")
    private Boolean supportsBookLateReturnPenaltyFee = false;

    @Column(name = "supports_scratch_card", nullable = false, columnDefinition = "boolean default false")
    private Boolean supportsScratchCard = false;

    @Column(name = "supports_referral", nullable = false, columnDefinition = "boolean default false")
    private Boolean supportsReferral = false;

    @Column(name = "supports_entry_exam", columnDefinition = "boolean default false")
    private Boolean supportsEntryExam = false; // New field for entry exam support

    private BigDecimal applicationFeeAmount;

    private BigDecimal bookLateReturnPenaltyFee;

    private BigDecimal libraryBookLateReturnFee;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime subscriptionExpiryDate;

    @Column(nullable = false)
    private Boolean isActive = false;

    private BigDecimal referralAmountPerPoint;

    @Column(columnDefinition = "int default 5")
    private Integer scratchCardMaxUsageCount = 5; // New field for max usage count

    private BigDecimal scratchCardPrice; // New field for scratch card price

    @ManyToMany
    @JoinTable(
            name = "school_services",
            joinColumns = @JoinColumn(name = "school_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    private List<ServiceOffered> selectedServices = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "social_media_links", joinColumns = @JoinColumn(name = "school_id"))
    @MapKeyColumn(name = "platform")
    @Column(name = "url")
    private Map<String, String> socialMediaLinks = new HashMap<>();

    @JsonManagedReference
    @OneToMany(mappedBy = "school", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User> users;

    @OneToOne(mappedBy = "school", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private Wallet wallet;


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "payment_account_id")
    private PaymentAccount paymentAccount;

    @Column(nullable = false)
    private Integer numberOfStudents = 0;

    @Column(nullable = false)
    private Integer actualNumberOfStudents = 0;

    @Column(nullable = false)
    private Integer actualNumberOfStaff = 0;

    private LocalDateTime lastRenewalDate;

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
        return subscriptionExpiryDate != null &&
                subscriptionExpiryDate.isAfter(LocalDateTime.now());
    }

    public void incrementActualNumberOfStudents() {
        this.actualNumberOfStudents++;
    }

    public void incrementActualNumberOfStaff() {
        this.actualNumberOfStaff++;
    }

    public void decrementActualNumberOfStudents() {
        if (this.actualNumberOfStudents > 0) {
            this.actualNumberOfStudents--;
        }
    }
}
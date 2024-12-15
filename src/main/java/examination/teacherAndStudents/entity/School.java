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
import java.util.Collection;
import java.util.Collections;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "school")
@Entity
@Builder
public class School  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String schoolName;
    private String schoolAddress;

    private String schoolLogoUrl;

    private String phoneNumber;

    @Column(unique = true)
    private String subscriptionKey;
//    @OneToMany(mappedBy = "school")
//   private List<User> students;

    @ElementCollection
    private List<String> selectedServices; // Store selected services by their names

    private LocalDate subscriptionExpiryDate;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;


    public Collection<Object> selectedServices() {
        return Collections.singleton(this.selectedServices);
    }
}

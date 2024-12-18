package examination.teacherAndStudents.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "transport_tracker")
public class TransportTracker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "transport_id", unique = true, nullable = false)
    private Transport transport;

    @Column(name = "remaining_capacity", nullable = false)
    private int remainingCapacity;

    public void assignStudent() {
        if (remainingCapacity <= 0) {
            throw new IllegalStateException("No available capacity");
        }
        remainingCapacity--;
    }

    public void removeStudent() {
        remainingCapacity++;
    }
}
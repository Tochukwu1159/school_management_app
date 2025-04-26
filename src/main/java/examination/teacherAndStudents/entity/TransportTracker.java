package examination.teacherAndStudents.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "transport_tracker")
public class TransportTracker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private BusRoute busRoute;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private AcademicSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "term_id", nullable = false)
    private StudentTerm term;

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
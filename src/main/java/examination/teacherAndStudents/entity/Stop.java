package examination.teacherAndStudents.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Getter
@Setter@AllArgsConstructor
@NoArgsConstructor
@Table(name = "stop")
@Entity
@Builder
public class Stop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stopId;

    @Column(nullable = false)
    private String stopName;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private int sequenceOrder;

    @Column(nullable = false)
    private LocalTime arrivalTime;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private BusRoute route;
}
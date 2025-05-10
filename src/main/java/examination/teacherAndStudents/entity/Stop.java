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
    private String address;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private BusRoute route;

    // Add geolocation fields
    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;
}
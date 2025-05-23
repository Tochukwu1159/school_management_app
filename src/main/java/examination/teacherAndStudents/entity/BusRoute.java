package examination.teacherAndStudents.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "bus_route")
@Entity
@Builder
public class BusRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String routeName;

    @Column(nullable = false)
    private String startPoint;

    @Column
    private Double startLatitude;

    @Column
    private Double startLongitude;

    @Column(nullable = false)
    private String endPoint;

    @Column
    private Double endLatitude;

    @Column
    private Double endLongitude;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<Stop> stops = new ArrayList<>();

    @OneToMany(mappedBy = "busRoute", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Bus> buses = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void addStop(Stop stop) {
        stops.add(stop);
        stop.setRoute(this);
    }

    public void removeStop(Stop stop) {
        stops.remove(stop);
        stop.setRoute(null);
    }

    public void addBus(Bus bus) {
        buses.add(bus);
        bus.setBusRoute(this);
    }

    public void removeBus(Bus bus) {
        buses.remove(bus);
        bus.setBusRoute(null);
    }
}
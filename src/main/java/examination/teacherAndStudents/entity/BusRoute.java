package examination.teacherAndStudents.entity;
import examination.teacherAndStudents.utils.BorrowingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "bus_route")
@Entity
@Builder
public class BusRoute extends BaseEntity{
    private String routeName;
    private String startPoint;
    private String endPoint;
    @OneToOne(mappedBy = "busRoute", cascade = CascadeType.ALL)
    private Transport transport;
    // other fields, constructors, getters, setters
}

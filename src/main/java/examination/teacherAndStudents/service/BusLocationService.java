package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.BusLocationRequest;
import examination.teacherAndStudents.entity.BusLocation;

public interface BusLocationService {
    String updateBusLocation(BusLocationRequest request);
    BusLocation getBusLocation(Long busId);
}
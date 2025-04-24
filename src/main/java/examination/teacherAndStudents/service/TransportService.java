package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.entity.StudentTransportAllocation;
import org.springframework.data.domain.Page;

import java.util.List;

public interface TransportService {
    TransportResponse createTransport(TransportRequest transportRequest);

    TransportResponse updateTransport(Long transportId, TransportRequest updatedTransport);

    void deleteTransport(Long transportId);
    TransportAllocationResponse payForTransport(TransportPaymentRequest request) ;

    Page<TransportResponse> getAllTransports(
            Long id,
            String vehicleNumber,
            String licenceNumber,
            Long driverId,
            Boolean available,
            int page,
            int size,
            String sortBy,
            String sortDirection);
    TransportAllocationResponse assignTransportToStudent(AddStudentToTransportRequest addStudentToTransportRequest); ;

    TransportResponse addStudentsToTransport(Long transportId, List<Long> studentIds);

    TransportResponse getTransportById(Long transportId);

    TransportAllocationResponse removeStudentFromTransport(Long transportId, Long studentId);
    TransportResponse addBusToRoute(AddBusToRouteRequest request);
}
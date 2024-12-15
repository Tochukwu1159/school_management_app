package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.TransportRequest;
import examination.teacherAndStudents.dto.TransportResponse;
import examination.teacherAndStudents.entity.Transport;

import java.util.List;

public interface TransportService {
    TransportResponse createTransport(TransportRequest transportRequest);

    TransportResponse updateTransport(Long transportId, TransportRequest updatedTransport);

    void deleteTransport(Long transportId);

    List<TransportResponse> getAllTransports();
    TransportResponse addStudentToTransport(Long transportId, Long studentId);

    TransportResponse addStudentsToTransport(Long transportId, List<Long> studentIds);

    TransportResponse getTransportById(Long transportId);
}
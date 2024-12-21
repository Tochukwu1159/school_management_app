package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.TransportRequest;
import examination.teacherAndStudents.dto.TransportResponse;
import examination.teacherAndStudents.entity.StudentTransportAllocation;
import examination.teacherAndStudents.entity.Transport;

import java.util.List;

public interface TransportService {
    TransportResponse createTransport(TransportRequest transportRequest);

    TransportResponse updateTransport(Long transportId, TransportRequest updatedTransport);

    void deleteTransport(Long transportId);
    StudentTransportAllocation payForTransport(Long dueId, Long sessionId, Long termId);

    List<TransportResponse> getAllTransports();
    TransportResponse addStudentToTransport(Long transportTrackerId, Long studentId, Long transportId,
                                            Long academicYearId, Long termId);

    TransportResponse addStudentsToTransport(Long transportId, List<Long> studentIds);

    TransportResponse getTransportById(Long transportId);

    TransportResponse removeStudentFromTransport(Long transportId, Long studentId);
}
package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.StudentManifestRequest;
import examination.teacherAndStudents.dto.StudentManifestResponse;
import org.springframework.data.domain.Page;

public interface StudentManifestService {
    StudentManifestResponse createOrUpdateManifest(StudentManifestRequest request);
    StudentManifestResponse getManifestById(Long id);
    Page<StudentManifestResponse> getManifestsByTripId(Long routeId, int page, int size, String sortBy, String sortDirection,
                                                       Long academicSessionId, Long studentTermId, Long profileId, String status);

    void deleteManifest(Long id);
}
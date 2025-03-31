package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.HostelRequest;
import examination.teacherAndStudents.dto.HostelResponse;
import examination.teacherAndStudents.entity.Hostel;
import examination.teacherAndStudents.utils.AvailabilityStatus;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface HostelService {
    Page<HostelResponse> getAllHostels(
            String hostelName,
            AvailabilityStatus availabilityStatus,
            Long id,
            int page,
            int size,
            String sortBy,
            String sortDirection);
    HostelResponse getHostelById(Long hostelId);
    HostelResponse createHostel(HostelRequest hostelRequest);
    HostelResponse updateHostel(Long hostelId, HostelRequest updatedHostel);
    void deleteHostel(Long hostelId);

}

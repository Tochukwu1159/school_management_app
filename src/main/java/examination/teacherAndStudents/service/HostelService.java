package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.HostelRequest;
import examination.teacherAndStudents.entity.Hostel;

import java.util.List;
import java.util.Optional;

public interface HostelService {
    List<Hostel> getAllHostels();
    Optional<Hostel> getHostelById(Long hostelId);
    Hostel createHostel(HostelRequest hostel);
    Hostel updateHostel(Long hostelId, HostelRequest updatedHostel);
    void deleteHostel(Long hostelId);
    List<Hostel> getAllAvailableHostels();

}

package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.HostelRequest;
import examination.teacherAndStudents.dto.HostelResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.*;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.HostelService;
import examination.teacherAndStudents.utils.AvailabilityStatus;
import examination.teacherAndStudents.utils.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HostelServiceImpl implements HostelService {

    private final HostelRepository hostelRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final HostelBedTrackerRepository hostelBedTrackerRepository;
    private final AcademicSessionRepository academicSessionRepository;
    private final HostelAllocationRepository hostelAllocationRepository;

    @Override
    public Page<HostelResponse> getAllHostels(
            String hostelName,
            AvailabilityStatus availabilityStatus,
            Long id,
            int page,
            int size,
            String sortBy,
            String sortDirection) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("User not found"));

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Hostel> hostelsPage = hostelRepository.findAllBySchoolWithFilters(
                user.getSchool().getId(),
                hostelName,
                availabilityStatus,
                id,
                pageable);

        return hostelsPage.map(this::mapToHostelResponse);
    }

    @Override
    public HostelResponse getHostelById(Long hostelId) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("User not found"));

        Hostel hostel = hostelRepository.findByIdAndSchoolId(hostelId, user.getSchool().getId())
                .orElseThrow(() -> new CustomNotFoundException("Hostel not found with ID: " + hostelId));

        return mapToHostelResponse(hostel);
    }

    @Override
    @Transactional
    public HostelResponse createHostel(HostelRequest hostelRequest) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User user = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                .orElseThrow(() -> new AuthenticationFailedException("Please login as an Admin"));

        if (hostelRequest.getNumberOfBed() <= 0) {
            throw new IllegalArgumentException("Number of beds must be greater than zero");
        }
        if (hostelRequest.getCostPerBed().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Cost per bed must be greater than zero");
        }

        Profile warden = null;
        if (hostelRequest.getWardenId() != null) {
            warden = profileRepository.findById(hostelRequest.getWardenId())
                    .orElseThrow(() -> new CustomNotFoundException("Warden not found with ID: " + hostelRequest.getWardenId()));
        }

        Hostel newHostel = Hostel.builder()
                .hostelName(hostelRequest.getHostelName())
                .costPerBed(hostelRequest.getCostPerBed())
                .numberOfBed(hostelRequest.getNumberOfBed())
                .description(hostelRequest.getDescription())
                .availabilityStatus(AvailabilityStatus.AVAILABLE)
                .warden(warden)
                .school(user.getSchool())
                .build();

        Hostel savedHostel = hostelRepository.save(newHostel);
        return mapToHostelResponse(savedHostel);
    }

    @Override
    @Transactional
    public HostelResponse updateHostel(Long hostelId, HostelRequest updatedHostel) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User user = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                .orElseThrow(() -> new AuthenticationFailedException("Please login as an Admin"));

        Hostel hostel = hostelRepository.findByIdAndSchoolId(hostelId, user.getSchool().getId())
                .orElseThrow(() -> new CustomNotFoundException("Hostel not found with ID: " + hostelId));

        if (updatedHostel.getNumberOfBed() <= 0) {
            throw new IllegalArgumentException("Number of beds must be greater than zero");
        }
        if (updatedHostel.getCostPerBed().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Cost per bed must be greater than zero");
        }

        Profile warden = null;
        if (updatedHostel.getWardenId() != null) {
            warden = profileRepository.findById(updatedHostel.getWardenId())
                    .orElseThrow(() -> new CustomNotFoundException("Warden not found with ID: " + updatedHostel.getWardenId()));
        }

        hostel.setHostelName(updatedHostel.getHostelName());
        hostel.setCostPerBed(updatedHostel.getCostPerBed());
        hostel.setNumberOfBed(updatedHostel.getNumberOfBed());
        hostel.setWarden(warden);

        // Update availability status based on bed tracker (if exists)
        Optional<HostelBedTracker> tracker = hostelBedTrackerRepository.findByHostelAndAcademicYear(
                hostel, academicSessionRepository.findCurrentSession(user.getSchool().getId()).orElseThrow(() -> new CustomNotFoundException("Academic Year not found")));
        if (tracker.isPresent() && tracker.get().getNumberOfBedLeft() == 0) {
            hostel.setAvailabilityStatus(AvailabilityStatus.UNAVAILABLE);
        } else {
            hostel.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        }

        Hostel updated = hostelRepository.save(hostel);
        return mapToHostelResponse(updated);
    }

    @Override
    @Transactional
    public void deleteHostel(Long hostelId) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User user = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                .orElseThrow(() -> new AuthenticationFailedException("Please login as an Admin"));

        Hostel hostel = hostelRepository.findByIdAndSchoolId(hostelId, user.getSchool().getId())
                .orElseThrow(() -> new CustomNotFoundException("Hostel not found with ID: " + hostelId));

        if (hostelAllocationRepository.existsByHostel(hostel)) {
            throw new IllegalStateException("Cannot delete hostel with active allocations");
        }

        hostelRepository.delete(hostel);
    }

    private HostelResponse mapToHostelResponse(Hostel hostel) {
        return HostelResponse.builder()
                .id(hostel.getId())
                .hostelName(hostel.getHostelName())
                .numberOfBed(hostel.getNumberOfBed())
                .costPerBed(hostel.getCostPerBed())
                .availabilityStatus(hostel.getAvailabilityStatus())
                .schoolId(hostel.getSchool().getId())
                .schoolName(hostel.getSchool().getSchoolName())
                .wardenId(hostel.getWarden() != null ? hostel.getWarden().getId() : null)
                .build();
    }
}
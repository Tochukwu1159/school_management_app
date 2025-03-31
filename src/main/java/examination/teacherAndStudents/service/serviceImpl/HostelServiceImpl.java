package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.HostelRequest;
import examination.teacherAndStudents.dto.HostelResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.*;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.HostelService;
import examination.teacherAndStudents.utils.AvailabilityStatus;
import examination.teacherAndStudents.utils.NotificationType;
import examination.teacherAndStudents.utils.Roles;
import examination.teacherAndStudents.utils.TransactionType;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class HostelServiceImpl implements HostelService {

    @Autowired
    private HostelRepository hostelRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProfileRepository profileRepository;

    @Override
    public Page<HostelResponse> getAllHostels(
            String hostelName,
            AvailabilityStatus availabilityStatus,
            Long id,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User attendant = userRepository.findByEmailAndRoles(email, Roles.ADMIN)
                    .orElseThrow(() -> new CustomNotFoundException("Admin not found"));

            Profile attendantProfile = profileRepository.findByUser(attendant)
                    .orElseThrow(() -> new CustomNotFoundException("Attendant profile not found"));

            // Create Pageable object
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            // Fetch filtered hostels
            Page<Hostel> hostelsPage = hostelRepository.findAllBySchoolWithFilters(
                    attendantProfile.getUser().getSchool().getId(),
                    hostelName,
                    availabilityStatus,
                    id,
                    pageable);

            // Map to response DTO
            return hostelsPage.map(this::mapToHostelResponse);
        } catch (CustomNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomInternalServerException("Error fetching all hostels: " + e.getMessage());
        }
    }


    @Override
    public HostelResponse getHostelById(Long hostelId) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new CustomNotFoundException("User not found"));

            Hostel hostel = hostelRepository.findById(hostelId)
                    .orElseThrow(() -> new CustomNotFoundException("Hostel not found"));

            return mapToHostelResponse(hostel);
        } catch (CustomNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomInternalServerException("Error fetching hostel: " + e.getMessage());
        }
    }


    @Override
    public HostelResponse createHostel(HostelRequest hostelRequest) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new CustomNotFoundException("User not found"));

            if (user.getRoles() != Roles.ADMIN) {
                throw new AuthenticationFailedException("Only admins can create hostels");
            }

            Hostel newHostel = Hostel.builder()
                    .numberOfBed(hostelRequest.getNumberOfBed())
                    .hostelName(hostelRequest.getHostelName())
                    .school(user.getSchool())
                    .costPerBed(hostelRequest.getCostPerBed())
                    .availabilityStatus(AvailabilityStatus.AVAILABLE)
                    .build();

            Hostel savedHostel = hostelRepository.save(newHostel);
            return mapToHostelResponse(savedHostel);
        } catch (CustomNotFoundException e) {
            throw new CustomInternalServerException("Error creating hostel: ");
        } catch (Exception e) {
            throw new CustomInternalServerException("Error creating hostel: " + e.getMessage());
        }
    }


    @Override
    public HostelResponse updateHostel(Long hostelId, HostelRequest updatedHostel) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new CustomNotFoundException("User not found"));

            if (user.getRoles() != Roles.ADMIN) {
                throw new AuthenticationFailedException("Only admins can update hostels");
            }

            Hostel hostel = hostelRepository.findById(hostelId)
                    .orElseThrow(() -> new CustomNotFoundException("Hostel not found"));

            hostel.setHostelName(updatedHostel.getHostelName());
            hostel.setCostPerBed(updatedHostel.getCostPerBed());
            hostel.setNumberOfBed(updatedHostel.getNumberOfBed());

            Hostel updated = hostelRepository.save(hostel);
            return mapToHostelResponse(updated);
        } catch ( CustomNotFoundException e) {
            throw new CustomInternalServerException("Error updating hostel: ");
        } catch (Exception e) {
            throw new CustomInternalServerException("Error updating hostel: " + e.getMessage());
        }
    }


    @Override
    public void deleteHostel(Long hostelId) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            Optional<User> optionalUser = userRepository.findByEmail(email);

            if (optionalUser.isPresent()) {
                User user = optionalUser.get();

                if (user.getRoles() == Roles.ADMIN) {
                    // Retrieve the hostel from the database
                    Optional<Hostel> existingHostel = hostelRepository.findById(hostelId);

                    if (existingHostel.isPresent()) {
                        hostelRepository.deleteById(hostelId);
                    } else {
                        throw new EntityNotFoundException("Hostel not found with ID: " + hostelId);
                    }
                } else {
                    throw new CustomNotFoundException("Please log in as an Admin");
                }
            } else {
                throw new EntityNotFoundException("User not found");
            }
        } catch (Exception e) {
            throw new CustomInternalServerException("Error occurred while deleting the hostel with ID: " + hostelId + ". " + e.getMessage());
        }
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
                .build();
    }

}
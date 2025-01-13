package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.HostelRequest;
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

    @Override
    public List<Hostel> getAllHostels() {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            Optional<User> optionalUser = userRepository.findByEmail(email);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                if (user.getRoles() == Roles.ADMIN) {
                    // User is an admin, fetch all hostels
                    List<Hostel> hostels = hostelRepository.findAll();
                    return hostels;
                } else {
                    throw new CustomNotFoundException("Please log in as an Admin"); // Return unauthorized response for non-admin users
                }
            } else {
                throw new CustomNotFoundException("User not found"); // Handle the case where the user is not found
            }
        } catch (Exception e) {
            throw new CustomInternalServerException("Error fetching all hostels: " + e.getMessage());
        }
    }

    @Override
    public Optional<Hostel> getHostelById(Long hostelId) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            Optional<User> optionalUser = userRepository.findByEmail(email);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                if (user.getRoles() == Roles.ADMIN) {
                    return hostelRepository.findById(hostelId);
                } else {
                    throw new CustomNotFoundException("Please log in as an Admin"); // Return unauthorized response for non-admin users
                }
            } else {
                throw new CustomNotFoundException("User not found"); // Handle the case where the user is not found
            }
        } catch (Exception e) {
            throw new CustomInternalServerException("Error fetching all hostels: " + e.getMessage());
        }
    }

    public List<Hostel> getAllAvailableHostels() {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            Optional<User> optionalUser = userRepository.findByEmail(email);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                if (user.getRoles() == Roles.ADMIN) {
                    // User is an admin, fetch all hostels
                    List<Hostel> hostels = hostelRepository.findByAvailabilityStatus(AvailabilityStatus.AVAILABLE);
                    return hostels;
                } else {
                    throw new CustomNotFoundException("Please log in as an Admin"); // Return unauthorized response for non-admin users
                }
            } else {
                throw new CustomNotFoundException("User not found"); // Handle the case where the user is not found
            }
        } catch (Exception e) {
            throw new CustomInternalServerException("Error fetching all available hostels: " + e.getMessage());
        }
    }


    @Override
    public Hostel createHostel(HostelRequest hostel) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            Optional<User> optionalUser = userRepository.findByEmail(email);

            if (optionalUser.isPresent()) {
                User user = optionalUser.get();

                if (user.getRoles() == Roles.ADMIN) {
                    Hostel newHostel = new Hostel();
                    newHostel.setNumberOfBed(hostel.getNumberOfBed());
                    newHostel.setHostelName(hostel.getHostelName());
                    newHostel.setSchool(user.getSchool());
                    newHostel.setCostPerBed(hostel.getCostPerBed());
                    newHostel.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);

                    return hostelRepository.save(newHostel);
                } else {
                    throw new CustomNotFoundException("You do not have permission to create a hostel. Please log in as an Admin.");
                }
            } else {
                throw new CustomNotFoundException("User not found with email: " + email);
            }
        } catch (Exception e) {
            // Rethrow a more generic exception or handle it based on your application's requirements
            throw new CustomInternalServerException("An error occurred while creating the hostel. Please try again later.");
        }
    }


    @Override
    public Hostel updateHostel(Long hostelId, HostelRequest updatedHostel) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            Optional<User> optionalUser = userRepository.findByEmail(email);

            if (optionalUser.isPresent()) {
                User user = optionalUser.get();

                if (user.getRoles() == Roles.ADMIN) {
                    Optional<Hostel> optionalHostel = hostelRepository.findById(hostelId);

                    if (optionalHostel.isPresent()) {
                        Hostel hostel = optionalHostel.get();
                        hostel.setHostelName(updatedHostel.getHostelName());
                        hostel.setCostPerBed(updatedHostel.getCostPerBed());
                        hostel.setNumberOfBed(updatedHostel.getNumberOfBed());

                        return hostelRepository.save(hostel);
                    } else {
                        throw new CustomNotFoundException("Hostel not found for ID: " + hostelId);
                    }
                } else {
                    throw new CustomNotFoundException("Please log in as an Admin");
                }
            } else {
                throw new CustomNotFoundException("User not found");
            }
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
}
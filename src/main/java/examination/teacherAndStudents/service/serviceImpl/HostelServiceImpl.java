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

//    @Override
//    public void addStudentToHostel(Long studentId, Long hostelId) {
//        try {
//            String email = SecurityConfig.getAuthenticatedUserEmail();
//            Optional<User> optionalUser = userRepository.findByEmail(email);
//
//            if (optionalUser.isPresent()) {
//                User user = optionalUser.get();
//
//                if (user.getRoles() == Roles.ADMIN) {
//                    // Retrieve the student from the database
//                    User student = userRepository.findById(studentId)
//                            .orElseThrow(() -> new EntityNotFoundException("Student not found with ID: " + studentId));
//                    // Check if the student is already assigned to a hostel
//                    if (student.getHostel() != null) {
//                        throw new CustomNotFoundException("Student already assigned to a hostel bed");
//                    }
//
//                    // Retrieve the hostel from the database
//                    Hostel existingHostel = hostelRepository.findById(hostelId)
//                            .orElseThrow(() -> new EntityNotFoundException("Hostel not found with ID: " + hostelId));
//
//                    // Check if there are available beds in the hostel
//                    int availableBeds = existingHostel.getNumberOfBed();
//                    if (availableBeds > 0) {
//                        // Update the student's hostel association
//                        student.setHostel(existingHostel);
//                        userRepository.save(student);
//
//                        // Decrease the number of available beds
//                        existingHostel.setNumberOfBed(availableBeds - 1);
//
//                        // Update the AvailabilityStatus based on the number of available beds
//                        if (availableBeds - 1 == 0) {
//                            existingHostel.setAvailabilityStatus(AvailabilityStatus.UNAVAILABLE);
//                        } else {
//                            existingHostel.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
//                        }
//                        // Update the hostel in the database
//                        hostelRepository.save(existingHostel);
//                    } else {
//                        throw new CustomNotFoundException("No available beds in the hostel");
//                    }
//                } else {
//                    throw new AuthenticationFailedException("Please log in as an Admin");
//                }
//            } else {
//                throw new EntityNotFoundException("User not found with email: " + email);
//            }
//        } catch (Exception e) {
//            throw new CustomNotFoundException("Error adding student to hostel: " + e.getMessage());
//        }
//    }
//
//    @Transactional
//    public boolean payForHostel(Long hostelId) {
//        try {
//            String email = SecurityConfig.getAuthenticatedUserEmail();
//            Optional<User> user = userRepository.findByEmail(email);
//            if (user == null) {
//                throw new CustomNotFoundException("Please login");
//            }
//            // Fetch user wallet
//            Wallet userWallet = walletRepository.findByUserId(user.get().getId())
//                    .orElseThrow(() -> new EntityNotFoundException("Wallet not found for user with id: " + user.get().getId()));
//
//            // Fetch hostel details
//            Hostel hostel = hostelRepository.findById(hostelId)
//                    .orElseThrow(() -> new EntityNotFoundException("Hostel not found with id: " + hostelId));
//
//            // Check if there is sufficient balance in the wallet
//            BigDecimal costPerBed = new BigDecimal(hostel.getCostPerBed());
//            if (userWallet.getBalance().compareTo(costPerBed) >= 0 && hostel.getNumberOfBed() > 0) {
//                // Deduct the cost from the wallet balance
//                BigDecimal newBalance = userWallet.getBalance().subtract(costPerBed);
//                userWallet.setBalance(newBalance);
//                userWallet.setTotalMoneySent(userWallet.getTotalMoneySent().add(costPerBed));
//
//                // Reduce the number of beds in the hostel
//                int newNumberOfBeds = hostel.getNumberOfBed() - 1;
//                hostel.setNumberOfBed(newNumberOfBeds);
//
//                // Update the wallet and hostel
//                walletRepository.save(userWallet);
//                hostelRepository.save(hostel);
//
//                Transaction transaction = Transaction.builder()
//                        .transactionType(TransactionType.DEBIT.name())
//                        .user(user.get())
//                        .amount(new BigDecimal(String.valueOf(costPerBed)))
//                        .description("You have successfully paid " + costPerBed + " " + "for hostel accommodation")
//                        .build();
//                transactionRepository.save(transaction);
//
//                Notification notification = Notification.builder()
//                        .notificationType(NotificationType.DEBIT_NOTIFICATION)
//                        .user(user.get())
//                        .transaction(transaction)
//                        .message("You have paid ₦" + " " + costPerBed + " " + "for hostel accommodation")
//                        .build();
//                notificationRepository.save(notification);
//
//                return true; // Payment successful
//            } else {
//                throw new InsufficientBalanceException("Insufficient funds or no available beds.");
//            }
//        } catch (EntityNotFoundException e) {
//
//            throw new CustomNotFoundException("Error processing payment. Please try again later.");
//        } catch (Exception e) {
//            throw new CustomNotFoundException("An unexpected error occurred. Please try again later.");
//        }
//    }
//
}
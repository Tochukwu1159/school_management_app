package examination.teacherAndStudents.service.serviceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import examination.teacherAndStudents.Security.JwtUtil;
import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.*;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.PayStackPaymentService;
import examination.teacherAndStudents.service.SchoolService;
import examination.teacherAndStudents.utils.Roles;
import examination.teacherAndStudents.utils.ServiceType;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchoolServiceImpl implements SchoolService {

    private final SchoolRepository schoolRepository;
    private final PayStackPaymentService paymentService;
    private final ModelMapper modelMapper;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;
    private final ServiceOfferedRepository serviceOfferedRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    private static ProfileData apply(Profile profile) {
        ProfileData profileData = new ProfileData();
        profileData.setId(profile.getId());
        profileData.setPhoneNumber(profile.getPhoneNumber());
        profileData.setUniqueRegistrationNumber(profile.getUniqueRegistrationNumber());
        return profileData;
    }

    public SchoolResponse onboardSchool(SchoolRequest schoolRequest) {
        try {
            String generatedSubscriptionKey = UUID.randomUUID().toString();

            if (schoolRepository.existsByEmail(schoolRequest.getEmail())) {

                throw new UserAlreadyExistException("Email already exist");

            }

            if (schoolRepository.existsByPhoneNumber(schoolRequest.getPhoneNumber())) {

                throw new UserAlreadyExistException("Phone Number already exist");

            }
            if (schoolRepository.existsBySchoolName(schoolRequest.getSchoolName())) {

                throw new UserAlreadyExistException("School Name already exist");

            }

            // Map SchoolRequest to School
            School newSchool = modelMapper.map(schoolRequest, School.class);
            newSchool.setIsActive(false);
            newSchool.setSubscriptionKey(generatedSubscriptionKey);

            String encryptedPassword = passwordEncoder.encode(schoolRequest.getPassword());
            newSchool.setPassword(encryptedPassword);

            // Fetch and associate selected services
            List<ServiceOffered> services = serviceOfferedRepository.findAllById(schoolRequest.getSelectedServices());
            newSchool.setSelectedServices(services);

            // Save the school entity
            School savedSchool = schoolRepository.save(newSchool);

            return modelMapper.map(savedSchool, SchoolResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Error onboarding school: " + e.getMessage(), e);
        }
    }


    public SchoolLoginResponse loginSchool(LoginRequest loginRequest) {
        try {
//            Authentication authenticate = authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
//            );
//
//            if (!authenticate.isAuthenticated()) {
//                throw new UserPasswordMismatchException("Wrong email or password");
//            }


            Optional<School> optionalSchool = schoolRepository.findByEmail(loginRequest.getEmail());

            School school = optionalSchool.get();

//            SecurityContextHolder.getContext().setAuthentication(authenticate);
            String token = "Bearer " + jwtUtil.generateToken(loginRequest.getEmail(), school);

            // Create a UserDto object containing user details
            SchoolResponse schoolResponse = new SchoolResponse();
            schoolResponse.setSchoolName(school.getSchoolName());
            schoolResponse.setSchoolAddress(school.getSchoolAddress());
            schoolResponse.setEmail(school.getEmail());
            return new SchoolLoginResponse(token, schoolResponse);
        } catch (BadCredentialsException e) {
            // Handle the "Bad credentials" error here
            throw new AuthenticationFailedException("Wrong email or password");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    public void accessibleService(Long schoolId, String serviceName) {
        try {
            School school = schoolRepository.findById(schoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("School not found"));

            if (!school.isSubscriptionValid()) {
                throw new SubscriptionExpiredException("Subscription expired");
            }

            boolean serviceSubscribed = school.getSelectedServices().stream()
                    .anyMatch(service -> service.getName().equals(serviceName));
            if (!serviceSubscribed) {
                throw new SubscriptionExpiredException("Service not subscribed");
            }

            System.out.println("Access granted for " + serviceName + " service.");
        } catch (ResourceNotFoundException | SubscriptionExpiredException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error accessing service: " + e.getMessage(), e);
        }
    }

    public School subscribeSchool(Long schoolId, SubscriptionRequest subscriptionRequest) {
        try {
            // Find the school by ID
            School school = schoolRepository.findById(schoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));

            int amountInKobo;
            try {
                amountInKobo = subscriptionRequest.getAmount() * 100;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid amount format", e);
            }

            String email = school.getEmail();


            PayStackTransactionRequest payStackTransactionRequest = PayStackTransactionRequest.builder()
                    .email(email)
                    .amount(new BigDecimal(amountInKobo))
                    .build();

            // Initialize payment transaction
//            boolean paymentSuccessful = paymentService.initTransaction(payStackTransactionRequest).isStatus();
            boolean paymentSuccessful = true;

            if (paymentSuccessful) {
                // Get the current expiry date or default to now if no active subscription
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime currentExpiryDate = school.getSubscriptionExpiryDate() != null && school.getSubscriptionExpiryDate().isAfter(now)
                        ? school.getSubscriptionExpiryDate()
                        : now;

                // Calculate the new expiry date based on the subscription type
                LocalDateTime newExpiryDate;
                switch (subscriptionRequest.getSubscriptionType()) {
                    case MONTHLY:
                        newExpiryDate = currentExpiryDate.plusDays(30);
                        break;
                    case YEARLY:
                        newExpiryDate = currentExpiryDate.plusYears(1);
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported subscription type: " + subscriptionRequest.getSubscriptionType());
                }

                SubscriptionHistory subscriptionHistory = SubscriptionHistory.builder()
                        .school(school)
                        .subscriptionType(subscriptionRequest.getSubscriptionType())
                        .startDate(currentExpiryDate.toLocalDate())
                        .endDate(newExpiryDate.toLocalDate())
                        .amountPaid(new BigDecimal(subscriptionRequest.getAmount()))
//                        .paymentReference(payStackTransactionRequest.getPaymentReference()) // Assume you get this from the payment response
                        .build();
                subscriptionHistoryRepository.save(subscriptionHistory);

                // Update subscription details
                school.setSubscriptionExpiryDate(newExpiryDate);
                school.setIsActive(newExpiryDate.isAfter(now)); // Check if the subscription is valid
                school.setSubscriptionType(subscriptionRequest.getSubscriptionType());
                schoolRepository.save(school);

                return school;
            } else {
                throw new PaymentFailedException("Subscription payment failed");
            }
        } catch (ResourceNotFoundException e) {
            throw e; // Rethrow known exceptions to avoid wrapping them unnecessarily
        } catch (IllegalArgumentException e) {
            throw e; // Handle input errors directly
        } catch (Exception e) {
            throw new RuntimeException("Error renewing subscription: " + e.getMessage(), e);
        }
    }


    public School findBySubscriptionKey(String subscriptionKey) {
        try {
            return schoolRepository.findBySubscriptionKey(subscriptionKey);
        } catch (Exception e) {
            throw new RuntimeException("Error finding subscription key: " + e.getMessage(), e);
        }
    }

    public List<ServiceOffered> getSelectedServices(Long schoolId) {
        try {
            School school = schoolRepository.findById(schoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));
            return school.getSelectedServices();
        } catch (Exception e) {
            throw new RuntimeException("Error fetching selected services: " + e.getMessage(), e);
        }
    }

    public BigDecimal getAmountToSubscribe(Long schoolId) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));
        long numberOfUsers = school.getUsers().size();
        return BigDecimal.valueOf(numberOfUsers * 2000);
    }

    public boolean canAccessService(Long schoolId, Long serviceId) {

        ServiceOffered serviceOffered = serviceOfferedRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with ID: " + serviceId));

        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));

        return school.getSelectedServices().stream()
                .anyMatch(service -> service.getName().equalsIgnoreCase(serviceOffered.getName()));
    }


    public void deactivateExpiredSubscriptions() {
        try {
            List<School> schools = schoolRepository.findAll();
            for (School school : schools) {
                if (!school.isSubscriptionValid()) {
                    school.setIsActive(false);
                    schoolRepository.save(school);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error deactivating expired subscriptions: " + e.getMessage(), e);
        }
    }

    public boolean isValidSubscriptionKey(Long schoolId) {
        try {
            School school = schoolRepository.findById(schoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));
            if (!school.isSubscriptionValid()) {
                throw new SubscriptionExpiredException("Subscription expired. Please renew.");
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Error validating subscription key: " + e.getMessage(), e);
        }
    }

    public List<School> getAllSchools() {
        try {
            return schoolRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Error fetching all schools: " + e.getMessage(), e);
        }
    }

    public School getSchoolById(Long schoolId) {
        try {
            return schoolRepository.findById(schoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));
        } catch (Exception e) {
            throw new RuntimeException("Error fetching school by ID: " + e.getMessage(), e);
        }
    }

    public void deleteSchool(Long schoolId) {
        try {
            School school = schoolRepository.findById(schoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));
            schoolRepository.delete(school);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting school: " + e.getMessage(), e);
        }
    }

    public School updateSchool(Long schoolId, SchoolRequest schoolRequest) {
        try {
            School school = schoolRepository.findById(schoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));
            modelMapper.map(schoolRequest, school);
            schoolRepository.save(school);
            return school;
        } catch (Exception e) {
            throw new RuntimeException("Error updating school: " + e.getMessage(), e);
        }
    }


    public List<ProfileData> teacherProfilesForSchool(Long schoolId) {
        try {
            List<User> teachers = userRepository.findByRolesAndSchoolId(Roles.TEACHER, schoolId);

            // Extract their user IDs
            List<Long> teacherIds = teachers.stream()
                    .map(User::getId)
                    .collect(Collectors.toList());

            List<Profile> teacherProfiles = profileRepository.findByUserIdIn(teacherIds);

            // Map profiles to ProfileData format

            return teacherProfiles.stream()
                    .map(profile -> {
                        ProfileData profileData = new ProfileData();
                        profileData.setId(profile.getId());
                        profileData.setPhoneNumber(profile.getPhoneNumber());
                        profileData.setUniqueRegistrationNumber(profile.getUniqueRegistrationNumber());
                        return profileData;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error fetching teacher profiles for the school: " + e.getMessage(), e);
        }
    }

    public List<ProfileData> studentsProfilesForSchool(Long schoolId) {
        try {
            List<User> students = userRepository.findByRolesAndSchoolId(Roles.STUDENT, schoolId);

            // Extract their user IDs
            List<Long> studentIds = students.stream()
                    .map(User::getId)
                    .collect(Collectors.toList());

            List<Profile>studentProfiles = profileRepository.findByUserIdIn(studentIds);

            // Map profiles to ProfileData format

            return studentProfiles.stream()
                    .map(profile -> {
                        ProfileData profileData = new ProfileData();
                        profileData.setId(profile.getId());
                        profileData.setPhoneNumber(profile.getPhoneNumber());
                        profileData.setUniqueRegistrationNumber(profile.getUniqueRegistrationNumber());
                        return profileData;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error fetching student profiles for the school: " + e.getMessage(), e);
        }
    }

    public List<ProfileData> adminProfilesForSchool(Long schoolId) {
        try {
            List<User> admins = userRepository.findByRolesAndSchoolId(Roles.ADMIN, schoolId);

            // Extract their user IDs
            List<Long> teacherIds = admins.stream()
                    .map(User::getId)
                    .collect(Collectors.toList());

            List<Profile> adminProfiles = profileRepository.findByUserIdIn(teacherIds);

            // Map profiles to ProfileData format

            return adminProfiles.stream()
                    .map(profile -> {
                        ProfileData profileData = new ProfileData();
                        profileData.setId(profile.getId());
                        profileData.setPhoneNumber(profile.getPhoneNumber());
                        profileData.setUniqueRegistrationNumber(profile.getUniqueRegistrationNumber());
                        return profileData;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error fetching admin profiles for the school: " + e.getMessage(), e);
        }
    }

    public List<ProfileData> gateManProfilesForSchool(Long schoolId) {
        try {
            List<User> gateMen = userRepository.findByRolesAndSchoolId(Roles.GATEMAN, schoolId);

            // Extract their user IDs
            List<Long> gateMenIds = gateMen.stream()
                    .map(User::getId)
                    .collect(Collectors.toList());

            List<Profile> gateMenProfiles = profileRepository.findByUserIdIn(gateMenIds);

            // Map profiles to ProfileData format

            return gateMenProfiles.stream()
                    .map(SchoolServiceImpl::apply)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error fetching gate men profiles for the school: " + e.getMessage(), e);
        }
    }
}


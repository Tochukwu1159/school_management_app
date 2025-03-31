package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.*;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.PayStackPaymentService;
import examination.teacherAndStudents.service.SchoolService;
import examination.teacherAndStudents.utils.Roles;
import examination.teacherAndStudents.utils.SubscriptionType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.service.spi.ServiceException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchoolServiceImpl implements SchoolService {

    private final SchoolRepository schoolRepository;
    private final ModelMapper modelMapper;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;
    private final ServiceOfferedRepository serviceOfferedRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PayStackPaymentService paymentService;
    private final EmailTemplateService emailTemplateService;

    @Value("${amount_charged_per_student}")
    private String amountChargedPerStudent;

    private static ProfileData apply(Profile profile) {
        ProfileData profileData = new ProfileData();
        profileData.setId(profile.getId());
        profileData.setPhoneNumber(profile.getPhoneNumber());
        profileData.setUniqueRegistrationNumber(profile.getUniqueRegistrationNumber());
        return profileData;
    }

    @Transactional
    public SchoolResponse onboardSchool(SchoolRequest schoolRequest) {
        // Validate input
        Objects.requireNonNull(schoolRequest, "School request cannot be null");

        // Get authenticated admin with proper authorization check
        User admin = getAuthenticatedAdmin();

        // Validate school uniqueness
        validateSchoolUniqueness(schoolRequest);

        // Create and save school
        School newSchool = createSchoolEntity(schoolRequest);
        School savedSchool = schoolRepository.save(newSchool);

        // Associate admin with school
        associateAdminWithSchool(admin, savedSchool);

        // Send notifications
        emailTemplateService.sendOnboardingNotifications(savedSchool, admin);

        return convertToResponse(savedSchool);
    }

    private User getAuthenticatedAdmin() {
        String email = SecurityConfig.getAuthenticatedUserEmail();
     Optional<User> optionalUser =   userRepository.findByEmailAndRoles(email, Roles.ADMIN);
        return  optionalUser.get();
    }

    private void validateSchoolUniqueness(SchoolRequest request) {
        if (schoolRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistException("Email already exists");
        }
        if (schoolRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new UserAlreadyExistException("Phone number already exists");
        }
        if (schoolRepository.existsBySchoolName(request.getSchoolName())) {
            throw new UserAlreadyExistException("School name already exists");
        }
        if (schoolRepository.existsBySchoolIdentificationNumber(request.getSchoolIdentificationNumber())) {
            throw new UserAlreadyExistException("School identification number already exists");
        }
    }

    private School createSchoolEntity(SchoolRequest request) {
        School school = modelMapper.map(request, School.class);

        // Set default values
        school.setIsActive(false);

        // Set social media links if provided
        Optional.ofNullable(request.getSocialMediaLinks())
                .ifPresent(school::setSocialMediaLinks);

        // Fetch and associate services
        List<ServiceOffered> services = serviceOfferedRepository.findAllById(
                Optional.ofNullable(request.getSelectedServices())
                        .orElse(Collections.emptyList())
        );
        school.setSelectedServices(services);

        return school;
    }

    private void associateAdminWithSchool(User admin, School school) {
        admin.setSchool(school);
        userRepository.save(admin);
    }

    private SchoolResponse convertToResponse(School school) {
        return modelMapper.map(school, SchoolResponse.class);
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

    public School subscribeSchool(SubscriptionRequest subscriptionRequest) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();

            // Validate user existence
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
            Objects.requireNonNull(subscriptionRequest, "Subscription request cannot be null");

            // Find the school by ID
            School school = schoolRepository.findById(user.getSchool().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + user.getSchool().getId()));

            if (school.getActualNumberOfStudents() == null || school.getActualNumberOfStudents() <= 0) {
                throw new IllegalStateException("School has no students registered");
            }

            int amountInKobo;
            long totalAmount;
            try {
                 totalAmount = Math.multiplyExact(
                        school.getActualNumberOfStudents(),
                        Integer.parseInt(amountChargedPerStudent)
                );

                if (totalAmount <= 0) {
                    throw new IllegalStateException("Calculated amount must be greater than zero");
                }
                amountInKobo = Math.toIntExact(totalAmount);

            } catch (ArithmeticException e) {
                throw new IllegalArgumentException("Amount too large", e);
            }

            PayStackTransactionRequest payStackTransactionRequest = PayStackTransactionRequest.builder()
                    .email(school.getEmail())
                    .amount(new BigDecimal(amountInKobo))
                    .metadata(Map.of(
                            "schoolId", user.getSchool().getId().toString(),
                            "subscriptionType", subscriptionRequest.getSubscriptionType().name(),
                            "purpose", "SUBSCRIPTION"
                    ))
                    .build();

            // Initialize payment transaction
            PayStackTransactionResponse paymentSuccessful = paymentService.initTransaction(payStackTransactionRequest);
//            boolean paymentSuccessful = true;

            if (paymentSuccessful.isStatus()) {

                LocalDateTime now = LocalDateTime.now();
                LocalDateTime currentExpiryDate = Optional.ofNullable(school.getSubscriptionExpiryDate())
                        .filter(date -> date.isAfter(now))
                        .orElse(now);

                LocalDateTime newExpiryDate = calculateNewExpiryDate(currentExpiryDate, subscriptionRequest.getSubscriptionType());

                SubscriptionHistory subscriptionHistory = SubscriptionHistory.builder()
                        .school(school)
                        .subscriptionType(subscriptionRequest.getSubscriptionType())
                        .startDate(currentExpiryDate.toLocalDate())
                        .endDate(newExpiryDate.toLocalDate())
                        .amountPaid(new BigDecimal(totalAmount))
                        .paymentReference(paymentSuccessful.getData().getReference())
                        .paymentMethod("Paystack")
                        .transactionStatus("completed")
                        .build();
                subscriptionHistoryRepository.save(subscriptionHistory);

                // Update subscription details
                school.setSubscriptionExpiryDate(newExpiryDate);
                school.setIsActive(newExpiryDate.isAfter(now)); // Check if the subscription is valid
                school.setSubscriptionKey(generateSubscriptionKey());
                school.setSubscriptionType(subscriptionRequest.getSubscriptionType());
                schoolRepository.save(school);

                // Send confirmation email
                emailTemplateService.sendSubscriptionConfirmationEmail(school, subscriptionRequest, newExpiryDate,  amountInKobo);


                return school;
            } else {
                throw new PaymentFailedException("Subscription payment failed");
            }
        } catch (ResourceNotFoundException | IllegalArgumentException | PaymentFailedException e) {
            throw e; // Re-throw known exceptions
        } catch (Exception e) {
            throw new ServiceException("Error processing subscription: " + e.getMessage(), e);
        }
    }


    @Transactional
    public School renewSubscription(SubscriptionType subscriptionType) throws Exception {
        String email = SecurityConfig.getAuthenticatedUserEmail();

        // Validate user existence
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        Objects.requireNonNull(subscriptionType, "Subscription type cannot be null");

        // Retrieve school with lock
        School school = schoolRepository.findById(user.getSchool().getId())
                .orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + user.getSchool().getId()));

        // Validate school has students
        if (school.getActualNumberOfStudents() == null || school.getActualNumberOfStudents() <= 0) {
            throw new IllegalStateException("Cannot renew subscription - school has no students");
        }

        // Calculate amount
        int amountInKobo = calculateSubscriptionAmount(school.getActualNumberOfStudents());

        // Process payment
        PayStackTransactionResponse paymentResponse = processPayment(school, amountInKobo, subscriptionType);

        // Calculate new expiry date with remaining days
        LocalDateTime newExpiryDate = calculateNewExpiryWithRemainingDays(school, subscriptionType);

        // Save subscription history
        createSubscriptionHistory(school, subscriptionType, amountInKobo, paymentResponse, newExpiryDate);

        // Update school subscription
        updateSchoolSubscription(school, subscriptionType, newExpiryDate);

        // Send confirmation
        emailTemplateService.sendRenewalConfirmation(school, subscriptionType, newExpiryDate, amountInKobo);

        return school;
    }

    private int calculateSubscriptionAmount(int studentCount) {
        try {
            return Math.multiplyExact(studentCount, Integer.parseInt(amountChargedPerStudent));
        } catch (ArithmeticException e) {
            throw new IllegalStateException("Amount calculation overflow for " +
                    studentCount + " students", e);
        }
    }

    private LocalDateTime calculateNewExpiryWithRemainingDays(School school, SubscriptionType subscriptionType) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentExpiry = school.getSubscriptionExpiryDate();

        if (currentExpiry != null && currentExpiry.isAfter(now)) {
            // Calculate remaining days from current subscription
            long remainingDays = ChronoUnit.DAYS.between(now, currentExpiry);

            // Calculate base new expiry date from today
            LocalDateTime baseNewExpiry = calculateBaseExpiryDate(now, subscriptionType);

            // Add remaining days to the new expiry date
            return baseNewExpiry.plusDays(remainingDays);
        }

        // If expired or no current expiry, start from now
        return calculateBaseExpiryDate(now, subscriptionType);
    }

    private LocalDateTime calculateBaseExpiryDate(LocalDateTime startDate, SubscriptionType type) {
        return switch (type) {
            case MONTHLY -> startDate.plusMonths(1);
            case QUARTERLY -> startDate.plusMonths(3);
            case YEARLY -> startDate.plusYears(1);
        };
    }

    private PayStackTransactionResponse processPayment(School school, int amountInKobo, SubscriptionType type) throws Exception {
        PayStackTransactionRequest request = PayStackTransactionRequest.builder()
                .email(school.getEmail())
                .amount(new BigDecimal(amountInKobo))
                .metadata(Map.of(
                        "schoolId", school.getId().toString(),
                        "subscriptionType", type.name(),
                        "purpose", "SUBSCRIPTION_RENEWAL"
                ))
                .build();

        PayStackTransactionResponse response = paymentService.initTransaction(request);
        if (!response.isStatus()) {
            throw new PaymentFailedException("Payment failed: " + response.getMessage());
        }
        return response;
    }

    private void createSubscriptionHistory(School school, SubscriptionType type,
                                           int amountInKobo, PayStackTransactionResponse paymentResponse,
                                           LocalDateTime newExpiryDate) {
        SubscriptionHistory history = SubscriptionHistory.builder()
                .school(school)
                .subscriptionType(type)
                .startDate(LocalDate.now())
                .endDate(newExpiryDate.toLocalDate())
                .amountPaid(new BigDecimal(amountInKobo / 100.0))
                .paymentReference(paymentResponse.getData().getReference())
                .paymentMethod("Paystack")
                .transactionStatus("completed")
                .studentCount(school.getActualNumberOfStudents())
                .previousExpiryDate(school.getSubscriptionExpiryDate())
                .daysCarriedOver(calculateCarriedOverDays(school))
                .build();

        subscriptionHistoryRepository.save(history);
    }

    private long calculateCarriedOverDays(School school) {
        if (school.getSubscriptionExpiryDate() == null) {
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        return school.getSubscriptionExpiryDate().isAfter(now)
                ? ChronoUnit.DAYS.between(now, school.getSubscriptionExpiryDate())
                : 0;
    }

    private void updateSchoolSubscription(School school, SubscriptionType type, LocalDateTime newExpiryDate) {
        school.setSubscriptionExpiryDate(newExpiryDate);
        school.setIsActive(true);
        school.setSubscriptionType(type);
        school.setLastRenewalDate(LocalDateTime.now());
        schoolRepository.save(school);
    }

    private LocalDateTime calculateNewExpiryDate(LocalDateTime currentDate, SubscriptionType subscriptionType) {
        return switch (subscriptionType) {
            case MONTHLY -> currentDate.plusMonths(1);
            case QUARTERLY -> currentDate.plusMonths(3);
            case YEARLY -> currentDate.plusYears(1);
            default -> throw new IllegalArgumentException("Unsupported subscription type: " + subscriptionType);
        };
    }

    private String generateSubscriptionKey() {
        return UUID.randomUUID().toString();
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


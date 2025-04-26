package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.*;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.PayStackPaymentService;
import examination.teacherAndStudents.service.SchoolService;
import examination.teacherAndStudents.utils.AccountUtils;
import examination.teacherAndStudents.utils.Roles;
import examination.teacherAndStudents.utils.SubscriptionType;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of SchoolService for managing school operations.
 */
@Service
@RequiredArgsConstructor
public class SchoolServiceImpl implements SchoolService {

    private static final Logger logger = LoggerFactory.getLogger(SchoolServiceImpl.class);

    private final SchoolRepository schoolRepository;
    private final ModelMapper modelMapper;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;
    private final ServiceOfferedRepository serviceOfferedRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PayStackPaymentService paymentService;
    private final EmailTemplateService emailTemplateService;

    @Value("${amount_charged_per_student}")
    private BigDecimal amountChargedPerStudent;

    @Override
    @Transactional
    public SchoolResponse onboardSchool(SchoolRequest schoolRequest) {
        if (schoolRequest == null) {
            throw new BadRequestException("School request cannot be null.");
        }

        User admin = getAuthenticatedAdmin();
        validateSchoolUniqueness(schoolRequest);

        School school = createSchoolEntity(schoolRequest);
        School savedSchool = schoolRepository.save(school);

        associateAdminWithSchool(admin, savedSchool);
        emailTemplateService.sendOnboardingNotifications(savedSchool, admin);

        logger.info("Onboarded school ID {} with name {}", savedSchool.getId(), savedSchool.getSchoolName());
        return modelMapper.map(savedSchool, SchoolResponse.class);
    }

    private User getAuthenticatedAdmin() {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        return userRepository.findByEmailAndRole(email, Roles.ADMIN)
                .orElseThrow(() -> new AuthenticationFailedException("Admin access required for email: " + email));
    }

    private void validateSchoolUniqueness(SchoolRequest request) {
        if (schoolRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistException("Email already exists: " + request.getEmail());
        }
        if (schoolRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new UserAlreadyExistException("Phone number already exists: " + request.getPhoneNumber());
        }
        if (schoolRepository.existsBySchoolName(request.getSchoolName())) {
            throw new UserAlreadyExistException("School name already exists: " + request.getSchoolName());
        }
        if (schoolRepository.existsBySchoolIdentificationNumber(request.getSchoolIdentificationNumber())) {
            throw new UserAlreadyExistException("School identification number already exists: " + request.getSchoolIdentificationNumber());
        }
    }

    private School createSchoolEntity(SchoolRequest request) {
        School school = modelMapper.map(request, School.class);
        school.setIsActive(false);
        school.setSocialMediaLinks(request.getSocialMediaLinks() != null ? request.getSocialMediaLinks() : new HashMap<>());

        // Fetch default services and requested services
        List<ServiceOffered> defaultServices = serviceOfferedRepository.findByIsDefaultTrue();
        Set<Long> requestedServiceIds = request.getSelectedServices() != null ? new HashSet<>(request.getSelectedServices()) : new HashSet<>();

        // Combine default and requested services, avoiding duplicates
        Set<Long> allServiceIds = new HashSet<>(requestedServiceIds);
        defaultServices.forEach(service -> allServiceIds.add(service.getId()));

        List<ServiceOffered> services = serviceOfferedRepository.findAllById(allServiceIds);
        if (services.size() < allServiceIds.size()) {
            throw new BadRequestException("One or more selected services are invalid.");
        }

        school.setSelectedServices(services);
        school.setSchoolCode(AccountUtils.generateSchoolCode());
        logger.info("Assigned {} services to school (including {} default services)", services.size(), defaultServices.size());

        return school;
    }

    private void associateAdminWithSchool(User admin, School school) {
        admin.setSchool(school);
        userRepository.save(admin);
    }

    @Override
    public void accessibleService(Long schoolId, String serviceName) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));

        if (!school.isSubscriptionValid()) {
            throw new SubscriptionExpiredException("Subscription expired for school ID: " + schoolId);
        }

        boolean serviceSubscribed = school.getSelectedServices().stream()
                .anyMatch(service -> service.getName().equalsIgnoreCase(serviceName));
        if (!serviceSubscribed) {
            throw new SubscriptionExpiredException("Service '" + serviceName + "' not subscribed for school ID: " + schoolId);
        }
    }

    @Override
    @Transactional
    public School subscribeSchool(SubscriptionRequest subscriptionRequest) throws Exception {
        if (subscriptionRequest == null) {
            throw new BadRequestException("Subscription request cannot be null.");
        }

        User user = getAuthenticatedUser();
        School school = validateSchoolForSubscription(user.getSchool().getId());

        BigDecimal amount = calculateSubscriptionAmount(school.getActualNumberOfStudents());
        PayStackTransactionResponse paymentResponse = processPayment(school, amount, subscriptionRequest.getSubscriptionType(), "SUBSCRIPTION");

        LocalDateTime newExpiryDate = calculateNewExpiryDate(LocalDateTime.now(), subscriptionRequest.getSubscriptionType());
        createSubscriptionHistory(school, subscriptionRequest.getSubscriptionType(), amount, paymentResponse, newExpiryDate);

        updateSchoolSubscription(school, subscriptionRequest.getSubscriptionType(), newExpiryDate);
        emailTemplateService.sendSubscriptionConfirmationEmail(school, subscriptionRequest, newExpiryDate, amount.intValue());

        logger.info("Subscribed school ID {} with type {}", school.getId(), subscriptionRequest.getSubscriptionType());
        return school;
    }

    @Override
    @Transactional
    public School renewSubscription(SubscriptionType subscriptionType) throws Exception {
        if (subscriptionType == null) {
            throw new BadRequestException("Subscription type cannot be null.");
        }

        User user = getAuthenticatedUser();
        School school = validateSchoolForSubscription(user.getSchool().getId());

        BigDecimal amount = calculateSubscriptionAmount(school.getActualNumberOfStudents());
        PayStackTransactionResponse paymentResponse = processPayment(school, amount, subscriptionType, "SUBSCRIPTION_RENEWAL");

        LocalDateTime newExpiryDate = calculateNewExpiryWithRemainingDays(school, subscriptionType);
        createSubscriptionHistory(school, subscriptionType, amount, paymentResponse, newExpiryDate);

        updateSchoolSubscription(school, subscriptionType, newExpiryDate);
        emailTemplateService.sendRenewalConfirmation(school, subscriptionType, newExpiryDate, amount.intValue());

        logger.info("Renewed subscription for school ID {} with type {}", school.getId(), subscriptionType);
        return school;
    }

    private User getAuthenticatedUser() {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
    }

    private School validateSchoolForSubscription(Long schoolId) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));

        if (school.getActualNumberOfStudents() <= 0) {
            throw new BadRequestException("School has no students registered for ID: " + schoolId);
        }
        return school;
    }

    private BigDecimal calculateSubscriptionAmount(int studentCount) {
        if (studentCount <= 0) {
            throw new BadRequestException("Student count must be positive.");
        }
        return amountChargedPerStudent.multiply(BigDecimal.valueOf(studentCount));
    }

    private PayStackTransactionResponse processPayment(School school, BigDecimal amount, SubscriptionType type, String purpose) throws Exception {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Payment amount must be positive.");
        }

        PayStackTransactionRequest request = PayStackTransactionRequest.builder()
                .email(school.getEmail())
                .amount(amount)
                .metadata(Map.of(
                        "schoolId", school.getId().toString(),
                        "subscriptionType", type.name(),
                        "purpose", purpose
                ))
                .build();

        PayStackTransactionResponse response = paymentService.initTransaction(request);
        if (!response.isStatus()) {
            throw new PaymentFailedException("Payment failed: " + response.getMessage());
        }
        return response;
    }

    private LocalDateTime calculateNewExpiryWithRemainingDays(School school, SubscriptionType subscriptionType) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentExpiry = school.getSubscriptionExpiryDate();

        if (currentExpiry != null && currentExpiry.isAfter(now)) {
            long remainingDays = ChronoUnit.DAYS.between(now, currentExpiry);
            return calculateNewExpiryDate(now, subscriptionType).plusDays(remainingDays);
        }
        return calculateNewExpiryDate(now, subscriptionType);
    }

    private LocalDateTime calculateNewExpiryDate(LocalDateTime currentDate, SubscriptionType subscriptionType) {
        return switch (subscriptionType) {
            case MONTHLY -> currentDate.plusMonths(1);
            case QUARTERLY -> currentDate.plusMonths(3);
            case YEARLY -> currentDate.plusYears(1);
        };
    }

    private void createSubscriptionHistory(School school, SubscriptionType type, BigDecimal amount,
                                           PayStackTransactionResponse paymentResponse, LocalDateTime newExpiryDate) {
        SubscriptionHistory history = SubscriptionHistory.builder()
                .school(school)
                .subscriptionType(type)
                .startDate(LocalDate.now())
                .endDate(newExpiryDate.toLocalDate())
                .amountPaid(amount)
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
        LocalDateTime now = LocalDateTime.now();
        return school.getSubscriptionExpiryDate() != null && school.getSubscriptionExpiryDate().isAfter(now)
                ? ChronoUnit.DAYS.between(now, school.getSubscriptionExpiryDate())
                : 0;
    }

    private void updateSchoolSubscription(School school, SubscriptionType type, LocalDateTime newExpiryDate) {
        school.setSubscriptionExpiryDate(newExpiryDate);
        school.setIsActive(true);
        school.setSubscriptionType(type);
        school.setSubscriptionKey(UUID.randomUUID().toString());
        school.setLastRenewalDate(LocalDateTime.now());
        schoolRepository.save(school);
    }

    @Override
    public School findBySubscriptionKey(String subscriptionKey) {
        return schoolRepository.findBySubscriptionKey(subscriptionKey)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with subscription key: " + subscriptionKey));
    }

    @Override
    public List<ServiceOffered> getSelectedServices(Long schoolId) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));
        return school.getSelectedServices();
    }

    @Override
    public BigDecimal getAmountToSubscribe(Long schoolId) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));
        return calculateSubscriptionAmount(school.getActualNumberOfStudents());
    }

    @Override
    public boolean canAccessService(Long schoolId, Long serviceId) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));
        ServiceOffered service = serviceOfferedRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with ID: " + serviceId));

        return school.getSelectedServices().contains(service);
    }

    @Override
    @Transactional
    public void deactivateExpiredSubscriptions() {
        schoolRepository.findAll().forEach(school -> {
            if (!school.isSubscriptionValid()) {
                school.setIsActive(false);
                schoolRepository.save(school);
                logger.info("Deactivated expired subscription for school ID {}", school.getId());
            }
        });
    }

    @Override
    public boolean isValidSubscriptionKey(Long schoolId) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));
        if (!school.isSubscriptionValid()) {
            throw new SubscriptionExpiredException("Subscription expired for school ID: " + schoolId);
        }
        return true;
    }

    @Override
    public List<School> getAllSchools() {
        return schoolRepository.findAll();
    }

    @Override
    public School getSchoolById(Long schoolId) {
        return schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));
    }

    @Override
    @Transactional
    public void deleteSchool(Long schoolId) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));
        schoolRepository.delete(school);
        logger.info("Deleted school ID {}", schoolId);
    }

    @Override
    @Transactional
    public School updateSchool(Long schoolId, SchoolRequest schoolRequest) {
        if (schoolRequest == null) {
            throw new BadRequestException("School request cannot be null.");
        }

        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));
        modelMapper.map(schoolRequest, school);
        validateSchoolUniqueness(schoolRequest);
        School updatedSchool = schoolRepository.save(school);
        logger.info("Updated school ID {}", schoolId);
        return updatedSchool;
    }

    @Override
    public List<ProfileData> teacherProfilesForSchool(Long schoolId) {
        return getProfilesForRole(schoolId, Roles.TEACHER);
    }

    @Override
    public List<ProfileData> studentsProfilesForSchool(Long schoolId) {
        return getProfilesForRole(schoolId, Roles.STUDENT);
    }

    @Override
    public List<ProfileData> adminProfilesForSchool(Long schoolId) {
        return getProfilesForRole(schoolId, Roles.ADMIN);
    }

    @Override
    public List<ProfileData> gateManProfilesForSchool(Long schoolId) {
        return getProfilesForRole(schoolId, Roles.GATEMAN);
    }

    private List<ProfileData> getProfilesForRole(Long schoolId, Roles role) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));
        List<User> users = userRepository.findByRoleAndSchoolId(role, schoolId);
        List<Long> userIds = users.stream().map(User::getId).toList();
        List<Profile> profiles = profileRepository.findByUserIdIn(userIds);

        return profiles.stream()
                .map(profile -> new ProfileData(
                        profile.getId(),
                        profile.getPhoneNumber(),
                        profile.getUniqueRegistrationNumber()
                ))
                .toList();
    }
}
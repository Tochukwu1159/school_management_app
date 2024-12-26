package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.entity.School;
import examination.teacherAndStudents.entity.ServiceOffered;
import examination.teacherAndStudents.entity.SubscriptionHistory;
import examination.teacherAndStudents.error_handler.PaymentFailedException;
import examination.teacherAndStudents.error_handler.ResourceNotFoundException;
import examination.teacherAndStudents.error_handler.SubscriptionExpiredException;
import examination.teacherAndStudents.error_handler.UserAlreadyExistException;
import examination.teacherAndStudents.repository.SchoolRepository;
import examination.teacherAndStudents.repository.ServiceOfferedRepository;
import examination.teacherAndStudents.repository.SubscriptionHistoryRepository;
import examination.teacherAndStudents.service.PayStackPaymentService;
import examination.teacherAndStudents.service.SchoolService;
import examination.teacherAndStudents.utils.ServiceType;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SchoolServiceImpl implements SchoolService {

    private final SchoolRepository schoolRepository;
    private final PayStackPaymentService paymentService;
    private final ModelMapper modelMapper;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;
    private final ServiceOfferedRepository serviceOfferedRepository;

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
}

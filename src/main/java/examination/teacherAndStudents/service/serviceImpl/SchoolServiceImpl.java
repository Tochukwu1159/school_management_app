package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.FundWalletRequest;
import examination.teacherAndStudents.dto.PayStackTransactionRequest;
import examination.teacherAndStudents.dto.SchoolRequest;
import examination.teacherAndStudents.dto.SchoolResponse;
import examination.teacherAndStudents.entity.School;
import examination.teacherAndStudents.error_handler.PaymentFailedException;
import examination.teacherAndStudents.error_handler.ResourceNotFoundException;
import examination.teacherAndStudents.error_handler.SubscriptionExpiredException;
import examination.teacherAndStudents.repository.SchoolRepository;
import examination.teacherAndStudents.service.PayStackPaymentService;
import examination.teacherAndStudents.service.SchoolService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SchoolServiceImpl implements SchoolService {

    private final SchoolRepository schoolRepository;
    private final PayStackPaymentService paymentService;
    private final ModelMapper modelMapper;

    public SchoolResponse onboardSchool(SchoolRequest schoolRequest) {
        try {
            School newSchool = schoolRepository.save(modelMapper.map(schoolRequest, School.class));
            return modelMapper.map(newSchool, SchoolResponse.class);
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

            if (!school.getSelectedServices().contains(serviceName)) {
                throw new SubscriptionExpiredException("Service not subscribed");
            }
            System.out.println("Access granted for " + serviceName + " service.");
        } catch (ResourceNotFoundException | SubscriptionExpiredException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error accessing service: " + e.getMessage(), e);
        }
    }

    public School subscribeSchool(Long schoolId, LocalDate newExpiryDate) {
        try {
            School school = schoolRepository.findById(schoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));

            school.setSubscriptionExpiryDate(newExpiryDate);
            school.setIsActive(newExpiryDate.isAfter(LocalDate.now()));
            schoolRepository.save(school);
            return school;
        } catch (Exception e) {
            throw new RuntimeException("Error subscribing school: " + e.getMessage(), e);
        }
    }

    public School findBySubscriptionKey(String subscriptionKey) {
        try {
            return schoolRepository.findBySubscriptionKey(subscriptionKey);
        } catch (Exception e) {
            throw new RuntimeException("Error finding subscription key: " + e.getMessage(), e);
        }
    }

    public List<String> getSelectedServices(Long schoolId) {
        try {
            School school = schoolRepository.findById(schoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));
            return school.getSelectedServices();
        } catch (Exception e) {
            throw new RuntimeException("Error fetching selected services: " + e.getMessage(), e);
        }
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

    public void renewSubscription(Long schoolId, FundWalletRequest fundWalletRequest) {
        try {
            School school = schoolRepository.findById(schoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));

            int amountInKobo = Integer.parseInt(fundWalletRequest.getAmount()) * 100;

            String email = SecurityConfig.getAuthenticatedUserEmail();

            PayStackTransactionRequest payStackTransactionRequest = PayStackTransactionRequest.builder()
                    .email(email)
                    .amount(new BigDecimal(amountInKobo))
                    .build();

            boolean paymentSuccessful = paymentService.initTransaction(payStackTransactionRequest).isStatus();

            if (paymentSuccessful) {
                LocalDate newExpiryDate = LocalDate.now().plusMonths(1);
                school.setSubscriptionExpiryDate(newExpiryDate);
                school.setIsActive(true);
                schoolRepository.save(school);
            } else {
                throw new PaymentFailedException("Subscription payment failed");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount format");
        } catch (Exception e) {
            throw new RuntimeException("Error renewing subscription: " + e.getMessage(), e);
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

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
            throw new RuntimeException("Error onboarding school: " + e.getMessage());
        }
    }

    public void accessibleService(Long schoolId, String serviceName) {
        try {
            School school = schoolRepository.findById(schoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("School not found"));

            if (isSubscriptionExpired(school)) {
                throw new SubscriptionExpiredException("Subscription expired");
            }

            if (!school.getSelectedServices().contains(serviceName)) {
                throw new SubscriptionExpiredException("Service not subscribed");
            }
            System.out.println("Accessing " + serviceName + " service...");
        } catch (ResourceNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (SubscriptionExpiredException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }
    }
    //interceptors to check the sub key

    private boolean isSubscriptionExpired(School school) {
        LocalDate expiryDate = school.getSubscriptionExpiryDate();
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public School findBySubscriptionKey(String subscriptionKey) {
        return schoolRepository.findBySubscriptionKey(subscriptionKey);
    }

    public List<String> getSelectedServices(Long schoolId) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));
        return school.getSelectedServices();
    }

    public boolean isValidSubscriptionKey(String subscriptionKey) {
        School school = schoolRepository.findBySubscriptionKey(subscriptionKey);
        return school != null && school.getSubscriptionExpiryDate() != null && !school.getSubscriptionExpiryDate().isBefore(LocalDate.now());
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
                schoolRepository.save(school);
            } else {
                throw new PaymentFailedException("Subscription payment failed");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount format");
        } catch (ResourceNotFoundException e) {
            throw e; // Rethrow ResourceNotFoundException
        } catch (Exception e) {
            throw new RuntimeException("Error renewing subscription", e);
        }
    }

}

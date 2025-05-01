package examination.teacherAndStudents.service.serviceImpl;


import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.BadRequestException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.PaymentProcessingException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.ApplicationService;
import examination.teacherAndStudents.service.EmailService;
import examination.teacherAndStudents.service.funding.PaymentProvider;
import examination.teacherAndStudents.service.funding.PaymentProviderFactory;
import examination.teacherAndStudents.utils.*;
        import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    @Autowired
    private AdmissionApplicationRepository admissionApplicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ReferralRepository referralRepository;

    @Autowired
    private UserPointsRepository userPointsRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PaymentProviderFactory paymentProviderFactory;
    @Autowired
    private SchoolRepository schoolRepository;
    @Autowired
    private ClassBlockRepository classBlockRepository;

    @Transactional
    @Override
    public PaymentResponse payApplicationFee(Long applicationId, PaymentProviderRequest paymentRequest) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        Profile user = profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Unauthorized access - User privileges required"));

        AdmissionApplication application = admissionApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomNotFoundException("Application not found with ID: " + applicationId));

        if (application.getStatus() != ApplicationStatus.PAYMENT_PENDING) {
            throw new IllegalStateException("Application is not in payable state");
        }

        if (application.getApplicationFee().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("No application fee required for this application");
        }

        PaymentProvider paymentProvider = paymentProviderFactory.getProvider(paymentRequest.getProvider());
        PaymentRequestDto paymentRequestDto = new PaymentRequestDto(
                application.getProfile().getUser().getEmail(),
                application.getApplicationFee(),
                paymentRequest.getCallbackUrl(),
                createApplicationFeeMetadata(application)
        );

        PaymentInitResponse paymentResponse = paymentProvider.initiatePayment(paymentRequestDto);
        if (!paymentResponse.isStatus()) {
            throw new PaymentProcessingException("Payment initialization failed: " + paymentResponse.getMessage());
        }

        Payment payment = new Payment();
        payment.setAmount(application.getApplicationFee());
        payment.setReferenceNumber(paymentResponse.getReference());
        payment.setPaymentDate(LocalDate.now());
        payment.setMethod(PaymentMethod.BALANCE);
        payment.setPurpose(Purpose.APPLICATION_FEE);
        payment.setProvider(paymentRequest.getProvider());
        payment.setTransactionId(ReferenceGenerator.generateTransactionId("APP_FEE"));
        payment.setStatus(FeeStatus.PENDING);
        payment.setProfile(user);

        paymentRepository.save(payment);

        application.setStatus(ApplicationStatus.PAYMENT_IN_PROGRESS);
        admissionApplicationRepository.save(application);

        return PaymentResponse.builder()
                .authorizationUrl(paymentResponse.getAuthorizationUrl())
                .reference(paymentResponse.getReference())
                .amount(application.getApplicationFee())
                .build();
    }



    @Transactional
    @Override
    public ApplicationResponse reviewApplication(Long applicationId, ApplicationReviewDto review) {
        // 1. Fetch application and validate
        AdmissionApplication application = admissionApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomNotFoundException("Application not found"));

        // 2. Verify admin user
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                .orElseThrow(() -> new RuntimeException("Unauthorized access - Admin privileges required"));

        // 3. Validate application state
        if (application.getStatus() != ApplicationStatus.PENDING_REVIEW &&
                application.getStatus() != ApplicationStatus.DOCUMENTS_REQUIRED &&
                application.getStatus() != ApplicationStatus.PRE_APPROVED &&
                application.getStatus() != ApplicationStatus.EXAM_COMPLETED) {
            throw new BadRequestException("Application is not in reviewable state");
        }

        User applicant = application.getProfile().getUser();
        School school = admin.getSchool();

        // 4. Handle pre-approval for schools requiring entry exams
        if (school.getSupportsEntryExam() && review.getExamDate() != null &&
                application.getStatus() == ApplicationStatus.PENDING_REVIEW) {
            application.setExamDate(review.getExamDate());
            application.setStatus(ApplicationStatus.PRE_APPROVED);
            emailService.sendExamScheduleNotification(
                    applicant.getEmail(),
                    applicant.getFirstName(),
                    review.getExamDate(),
                    school
            );
        }
        // 5. Handle exam result update
        else if (school.getSupportsEntryExam() && application.getStatus() == ApplicationStatus.PRE_APPROVED &&
                review.getExamScore() >= 0) {
            application.setScore(review.getExamScore());
            application.setPassed(review.isPassed());
            application.setStatus(ApplicationStatus.EXAM_COMPLETED);
        }
        // 6. Process final review decision
        else {
            // Validate application fee if required
            if (school.getIsApplicationFee() && !application.isFeePaid()) {
                throw new BadRequestException("Application fee has not been paid");
            }

            // Validate exam results if school supports entry exams
            if (school.getSupportsEntryExam() && (!application.isPassed() || application.getScore() <= 0)) {
                throw new BadRequestException("Applicant has not passed the entry exam");
            }

            if (review.isApproved()) {
                handleApprovedApplication(application, applicant);
            } else if (review.isIncomplete()) {
                handleIncompleteApplication(application, review, applicant, school);
            } else if (review.isRejected()) {
                handleRejectedApplication(application, review, applicant, school);
            }
        }

        // 7. Save changes
        admissionApplicationRepository.save(application);
        userRepository.save(applicant);

       return ApplicationResponse.builder().reviewMessage("Application review completed").build();
    }

    private void handleApprovedApplication(AdmissionApplication application, User applicant) {
        if (applicant == null || application == null || applicant.getUserProfile() == null || applicant.getSchool() == null) {
            throw new IllegalArgumentException("Applicant, application, user profile, or school cannot be null");
        }

        // Update profile status and verification
        applicant.setProfileStatus(ProfileStatus.ACTIVE);
        applicant.setIsVerified(true);
        application.getProfile().setProfileStatus(ProfileStatus.ACTIVE);
        application.setStatus(ApplicationStatus.APPROVED);

        // Handle referral if it exists
        Referral referral = referralRepository.findByReferredUserAndStatus(application.getProfile(), ReferralStatus.PENDING);
        if (referral != null) {
            referral.setStatus(ReferralStatus.COMPLETED);
            referralRepository.save(referral);

            UserPoints userPoints = userPointsRepository.findByUser(referral.getReferringUser())
                    .orElseGet(() -> UserPoints.builder()
                            .user(referral.getReferringUser())
                            .points(0)
                            .build());

            userPoints.setPoints(userPoints.getPoints() + 1);
            userPointsRepository.save(userPoints);
        }

        // Increment class block student count
        ClassBlock classBlock = applicant.getUserProfile().getClassBlock();
        if (classBlock != null) {
            classBlock.setNumberOfStudents(classBlock.getNumberOfStudents() + 1);
            classBlockRepository.save(classBlock);
        }

        // Update school population
        School school = applicant.getSchool();
        school.incrementActualNumberOfStudents();
        schoolRepository.save(school);

        // Send admission confirmation email
        emailService.sendAdmissionConfirmation(
                applicant.getEmail(),
                applicant.getFirstName(),
                applicant.getMiddleName(), // Assuming middle name is optional; null if not available
                application.getApplicationNumber(),
                application.getSchool()
        );
    }

    private void handleIncompleteApplication(AdmissionApplication application,
                                             ApplicationReviewDto review,
                                             User applicant,
                                             School school) {
        application.setStatus(ApplicationStatus.DOCUMENTS_REQUIRED);
        if (review.getMissingDocuments() != null && !review.getMissingDocuments().isEmpty()) {
            application.setRequiredDocuments(review.getMissingDocuments());
        }

        emailService.sendDocumentsRequest(
                applicant.getEmail(),
                applicant.getFirstName(),
                review.getMissingDocuments(),
                school
        );
    }

    private void handleRejectedApplication(AdmissionApplication application,
                                           ApplicationReviewDto review,
                                           User applicant,
                                           School school) {
        applicant.setProfileStatus(ProfileStatus.REJECTED);
        application.getProfile().setProfileStatus(ProfileStatus.REJECTED);
        application.getProfile().getUser().setProfileStatus(ProfileStatus.REJECTED);
        application.setStatus(ApplicationStatus.REJECTED);
        application.setRejectionReason(review.getRejectionReason());

        emailService.sendRejectionNotification(
                applicant.getEmail(),
                applicant.getFirstName(),
                review.getRejectionReason(),
                school
        );
    }

    private Map<String, Object> createApplicationFeeMetadata(AdmissionApplication application) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("applicationNumber", application.getApplicationNumber());
        metadata.put("studentName", application.getProfile().getUser().getFirstName() + " " +
                application.getProfile().getUser().getLastName());
        metadata.put("paymentType", "APPLICATION_FEE");
        metadata.put("schoolId", application.getSchool().getId()); // Storing as Long instead of String
        metadata.put("academicSession", application.getSession().getName());
        metadata.put("amount", application.getApplicationFee()); // Adding amount as BigDecimal
        metadata.put("timestamp", LocalDateTime.now()); // Adding timestamp as Object
        return metadata;
    }


}
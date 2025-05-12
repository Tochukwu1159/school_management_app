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
    public ApplicationResponse reviewApplication(Long applicationId, ApplicationReviewDto review) {
        // 1. Fetch and validate application
        AdmissionApplication application = admissionApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomNotFoundException("Application not found"));

        // 2. Verify admin user
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                .orElseThrow(() -> new RuntimeException("Unauthorized access - Admin privileges required"));

        // 3. Validate application state
        if (!isReviewableState(application.getStatus())) {
            throw new BadRequestException("Application is not in a reviewable state");
        }

        User applicant = application.getProfile().getUser();
        School school = admin.getSchool();

        // 4. Handle entrance exam scheduling
        if (school.getSupportsEntryExam() &&
                application.getStatus() == ApplicationStatus.PENDING_REVIEW &&
                review.isApproved()) {
            LocalDateTime examDate = school.getExamDate();
            if (examDate == null) {
                throw new BadRequestException("School has no exam date configured");
            }
            application.setExamDate(examDate);
            application.setStatus(ApplicationStatus.EXAM_SCHEDULED);
            emailService.sendExamScheduleNotification(
                    applicant.getEmail(),
                    applicant.getFirstName(),
                    examDate,
                    school
            );
        }
        // 5. Handle exam result processing
        else if (school.getSupportsEntryExam() &&
                application.getStatus() == ApplicationStatus.EXAM_SCHEDULED &&
                review.getExamScore() >= 0) {
            application.setScore(review.getExamScore());
            application.setPassed(review.isPassed());
            application.setStatus(ApplicationStatus.EXAM_COMPLETED);
        }
        // 6. Process final review decision
        else {
            // Validate prerequisites
            validateApplicationPrerequisites(application, school);

            if (review.isApproved()) {
                handleApprovedApplication(application, applicant);
            } else if (review.isIncomplete()) {
                handleIncompleteApplication(application, review, applicant, school);
            } else if (review.isRejected()) {
                handleRejectedApplication(application, review, applicant, school);
            } else {
                throw new BadRequestException("Invalid review action");
            }
        }

        // 7. Save changes
        admissionApplicationRepository.save(application);
        userRepository.save(applicant);

        return ApplicationResponse.builder()
                .reviewMessage("Application review completed successfully")
                .build();
    }

    private boolean isReviewableState(ApplicationStatus status) {
        return status == ApplicationStatus.PENDING_REVIEW ||
                status == ApplicationStatus.DOCUMENTS_REQUIRED ||
                status == ApplicationStatus.EXAM_SCHEDULED ||
                status == ApplicationStatus.EXAM_COMPLETED;
    }

    private void validateApplicationPrerequisites(AdmissionApplication application, School school) {
        if (school.getIsApplicationFee() && !application.isFeePaid()) {
            throw new BadRequestException("Application fee has not been paid");
        }
        if (school.getSupportsEntryExam() &&
                (!application.isPassed() || application.getScore() <= 0)) {
            throw new BadRequestException("Applicant has not passed the entry exam");
        }
    }

    private void handleApprovedApplication(AdmissionApplication application, User applicant) {
        validateApplicationEntities(application, applicant);

        // Update statuses
        applicant.setProfileStatus(ProfileStatus.ACTIVE);
        applicant.setIsVerified(true);
        application.getProfile().setProfileStatus(ProfileStatus.ACTIVE);
        application.setStatus(ApplicationStatus.APPROVED);

        // Handle referral
        Referral referral = referralRepository.findByReferredUserAndStatus(
                application.getProfile(), ReferralStatus.PENDING);
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

        // Update class block
        ClassBlock classBlock = applicant.getUserProfile().getClassBlock();
        if (classBlock != null) {
            classBlock.setNumberOfStudents(classBlock.getNumberOfStudents() + 1);
            classBlockRepository.save(classBlock);
        }

        // Update school
        School school = applicant.getSchool();
        school.incrementActualNumberOfStudents();
        schoolRepository.save(school);

        // Send confirmation
        emailService.sendAdmissionConfirmation(
                applicant.getEmail(),
                applicant.getFirstName(),
                applicant.getMiddleName(),
                application.getApplicationNumber(),
                school
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
        if (review.getRejectionReason() == null || review.getRejectionReason().isEmpty()) {
            throw new BadRequestException("Rejection reason must be provided");
        }

        applicant.setProfileStatus(ProfileStatus.REJECTED);
        application.getProfile().setProfileStatus(ProfileStatus.REJECTED);
        application.setStatus(ApplicationStatus.REJECTED);
        application.setRejectionReason(review.getRejectionReason());

        emailService.sendRejectionNotification(
                applicant.getEmail(),
                applicant.getFirstName(),
                review.getRejectionReason(),
                school
        );
    }

    private void validateApplicationEntities(AdmissionApplication application, User applicant) {
        if (applicant == null ||
                application == null ||
                applicant.getUserProfile() == null ||
                applicant.getSchool() == null) {
            throw new IllegalArgumentException("Applicant, application, user profile, or school cannot be null");
        }
    }

    private Map<String, Object> createApplicationFeeMetadata(AdmissionApplication application) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("applicationNumber", application.getApplicationNumber());
        metadata.put("studentName", application.getProfile().getUser().getFirstName() + " " +
                application.getProfile().getUser().getLastName());
        metadata.put("paymentType", "APPLICATION_FEE");
        metadata.put("schoolId", application.getSchool().getId());
        metadata.put("academicSession", application.getSession().getSessionName().getName());
        metadata.put("amount", application.getApplicationFee());
        metadata.put("timestamp", LocalDateTime.now());
        return metadata;
    }


}
// approved condition(this sends pre-exam email if theschool suports it)

//{
//        "approved": true,
//        "rejected": false,
//        "incomplete": false,
//        "examScore": 0,
//        "passed": false,
//        "missingDocuments": [],
//        "rejectionReason": null
//        }

//result condition

// {
//   "approved": false,
//   "rejected": false,
//   "incomplete": false,
//   "examScore": 85,
//   "passed": true,
//   "missingDocuments": [],
//   "rejectionReason": null
// }

//after result condition

//{
//        "approved": true,
//        "rejected": false,
//        "incomplete": false,
//        "examScore": 0,
//        "passed": false,
//        "missingDocuments": [],
//        "rejectionReason": null
//        }

//rejection condition

// {
//   "approved": false,
//   "rejected": true,
//   "incomplete": false,
//   "examScore": 0,
//   "passed": false,
//   "missingDocuments": [],
//   "rejectionReason": "Did not meet final review criteria"
// }

//document required

// {
//   "approved": false,
//   "rejected": false,
//   "incomplete": true,
//   "examScore": 0,
//   "passed": false,
//   "missingDocuments": ["Transcript", "Recommendation Letter", "ID Copy"],
//   "rejectionReason": null
// }
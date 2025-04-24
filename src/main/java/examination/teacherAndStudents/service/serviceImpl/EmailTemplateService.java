package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.EmailDetails;
import examination.teacherAndStudents.dto.SubscriptionRequest;
import examination.teacherAndStudents.entity.School;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.EmailService;
import examination.teacherAndStudents.service.NotificationService;
import examination.teacherAndStudents.utils.Roles;
import examination.teacherAndStudents.utils.SubscriptionType;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EmailTemplateService {

    private final EmailService emailService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Value("${amount_charged_per_student}")
    private String amountChargedPerStudent;


    public void sendExpiryReminder(School school, int daysRemaining) throws MessagingException {
        Map<String, Object> model = new HashMap<>();
        model.put("schoolName", school.getSchoolName());
        model.put("expiryDate", school.getSubscriptionExpiryDate());
        model.put("daysRemaining", daysRemaining);
        model.put("contactEmail", school.getEmail());
        model.put("contactPhone", school.getPhoneNumber());

        String subject = String.format("Subscription Expiring in %d %s",
                daysRemaining,
                daysRemaining == 1 ? "day" : "days");

        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(school.getEmail())
                .subject(subject)
                .templateName("email-template-school-subscription-expired") // Thymeleaf template name
                .model(model)
                .build();

        emailService.sendHtmlEmail(emailDetails);

        // Optional: Also send to admin users of the school
        sendToSchoolAdmins(school, subject, "subscription-expiry-reminder-admin", model);
    }

    public void sendSubscriptionExpiredNotification(School school) throws MessagingException {
        Map<String, Object> model = new HashMap<>();
        model.put("schoolName", school.getSchoolName());
        model.put("expiryDate", school.getSubscriptionExpiryDate());
        model.put("contactEmail", "support@yourplatform.com"); // Your support email
        model.put("renewalLink", "https://yourplatform.com/renewal"); // Renewal URL

        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(school.getEmail())
                .subject("Your Subscription Has Expired")
                .templateName("email-template-school-subscription-reminder") // Thymeleaf template name
                .model(model)
                .build();

        emailService.sendHtmlEmail(emailDetails);

        // Optional: Also send to admin users with more urgent tone
        sendToSchoolAdmins(school, "URGENT: Subscription Expired", "subscription-expired-admin", model);
    }

    private void sendToSchoolAdmins(School school, String subject, String template, Map<String, Object> baseModel) {
        // Get all admin users for the school
        List<User> admins = userRepository.findBySchoolAndAnyRoles(
                school,
                Set.of(Roles.ADMIN)
        );

        for (User admin : admins) {
            try {
                Map<String, Object> adminModel = new HashMap<>(baseModel);
                adminModel.put("adminName", admin.getFirstName());

                EmailDetails adminEmail = EmailDetails.builder()
                        .recipient(admin.getEmail())
                        .subject(subject)
                        .templateName(template)
                        .model(adminModel)
                        .build();

                emailService.sendHtmlEmail(adminEmail);
            } catch (MessagingException e) {
                logger.error("Failed to send email to admin {}: {}", admin.getEmail(), e.getMessage());
            }
        }
    }

    public void sendSubscriptionConfirmationEmail(School school, SubscriptionRequest subscriptionRequest, LocalDateTime expiryDate, int amountInKobo) {
        try {
            Map<String, Object> model = new HashMap<>();
            model.put("schoolName", school.getSchoolName());
            model.put("subscriptionType", subscriptionRequest.getSubscriptionType());
            model.put("amountPaid", amountInKobo / 100.0);
            model.put("expiryDate", expiryDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
            model.put("receiptNumber", school.getSubscriptionKey());

            EmailDetails emailDetails = EmailDetails.builder()
                    .recipient(school.getEmail())
                    .subject("Subscription Confirmation - " + school.getSchoolName())
                    .templateName("subscription-confirmation")
                    .model(model)
                    .build();

            emailService.sendHtmlEmail(emailDetails);
        } catch (Exception e) {
            logger.error("Failed to send subscription confirmation email to {}", school.getEmail(), e);
        }
    }


    public void sendRenewalConfirmation(School school, SubscriptionType subscriptionType,
                                         LocalDateTime expiryDate, int amountInKobo) {
        try {
            Map<String, Object> emailModel = new HashMap<>();
            emailModel.put("schoolName", school.getSchoolName());
            emailModel.put("subscriptionType", subscriptionType);
            emailModel.put("amountPaid", amountInKobo / 100.0);
            emailModel.put("expiryDate", expiryDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
            emailModel.put("studentCount", school.getActualNumberOfStudents());
            emailModel.put("amountPerStudent", amountChargedPerStudent);

            EmailDetails emailDetails = EmailDetails.builder()
                    .recipient(school.getEmail())
                    .subject("Subscription Renewal Confirmation - " + school.getSchoolName())
                    .templateName("subscription-renewal-confirmation")
                    .model(emailModel)
                    .build();

            emailService.sendHtmlEmail(emailDetails);
        } catch (Exception e) {
            logger.error("Failed to send renewal confirmation to {}", school.getEmail(), e);
        }
    }

    public void sendOnboardingNotifications(School school, User admin) {
        try {
            // Send email to admin
            Map<String, Object> emailModel = new HashMap<>();
            emailModel.put("schoolName", school.getSchoolName());
            emailModel.put("adminName", admin.getFirstName() + " " + admin.getLastName());

            EmailDetails adminEmail = EmailDetails.builder()
                    .recipient(admin.getEmail())
                    .subject("School Onboarding Confirmation")
                    .templateName("school-onboarding-admin")
                    .model(emailModel)
                    .build();
            emailService.sendHtmlEmail(adminEmail);

            // Send system notification
            notificationService.createSystemNotification(
                    admin.getId(),
                    "School Onboarding Complete",
                    "You have successfully onboarded " + school.getSchoolName()
            );
        } catch (Exception e) {
            logger.error("Failed to send onboarding notifications", e);
        }
    }

}

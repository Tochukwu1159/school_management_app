package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.EmailDetails;
import examination.teacherAndStudents.dto.EmailDetailsToMultipleEmails;
import examination.teacherAndStudents.entity.AdmissionApplication;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.School;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender javaMailSender;
    @Autowired
    private SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String sendEmail;

    @Override
    public void sendHtmlEmail(EmailDetails emailDetails) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

        try {
            helper.setTo(emailDetails.getRecipient());
            helper.setSubject(emailDetails.getSubject());

            // Process the Thymeleaf template with the provided model
            Context context = new Context();
            context.setVariables(emailDetails.getModel());
            String htmlContent = templateEngine.process(emailDetails.getTemplateName(), context);

            helper.setText(htmlContent, true);
            javaMailSender.send(message);
            System.out.println("Email sent successfully.");
        } catch (MessagingException e) {
            System.err.println("Error sending email: " + e.getMessage());
        }
    }

    public void sendEmailWithThymeleaf(EmailDetails emailDetails) {
        Context context = new Context();
        context.setVariables(emailDetails.getModel());

        String emailContent = templateEngine.process(emailDetails.getTemplateName(), context);

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(sendEmail);
            helper.setTo(emailDetails.getRecipient());
            helper.setSubject(emailDetails.getSubject());
            helper.setText(emailContent, true);  // Set email content as HTML

            javaMailSender.send(mimeMessage);
            System.out.println("Email sent successfully");
        } catch (MessagingException e) {
            throw new CustomInternalServerException(e.getMessage());
        }
    }


    @Override
    public void sendToMultipleEmails(EmailDetailsToMultipleEmails emailDetails) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

        try {
            helper.setTo(emailDetails.getToEmails().toArray(new String[0]));
            helper.setSubject(emailDetails.getSubject());

            // Process the Thymeleaf template with the provided model
            Context context = new Context();
            context.setVariables(emailDetails.getModel());
            String htmlContent = templateEngine.process(emailDetails.getTemplateName(), context);

            helper.setText(htmlContent, true);
            javaMailSender.send(message);
            logger.info("Email sent successfully.");
        } catch (MessagingException e) {
            logger.error("Error sending email", e);
        }
    }

    public void sendEmails(EmailDetails emailDetails) {
        try {
            Context thymeleafContext = new Context();
            thymeleafContext.setVariables(emailDetails.getModel());

            String emailContent = templateEngine.process(emailDetails.getTemplateName(), thymeleafContext);

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);
            messageHelper.setFrom(sendEmail);
            messageHelper.setTo(emailDetails.getRecipient());
            messageHelper.setSubject(emailDetails.getSubject());
            messageHelper.setText(emailContent, true);

            javaMailSender.send(mimeMessage);

            System.out.println("Email sent successfully.");
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new CustomInternalServerException("Failed to send email: " + e.getMessage());
        }
    }

    public void sendEmailWithAttachment(EmailDetails emailDetails) {
        try {
            Context thymeleafContext = new Context();
            thymeleafContext.setVariables(emailDetails.getModel());

            String emailContent = templateEngine.process(emailDetails.getTemplateName(), thymeleafContext);

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);
            messageHelper.setFrom(sendEmail);
            messageHelper.setTo(emailDetails.getRecipient());
            messageHelper.setSubject(emailDetails.getSubject());
            messageHelper.setText(emailContent, true);

            if (emailDetails.getAttachmentPath() != null) {
                FileSystemResource attachment = new FileSystemResource(emailDetails.getAttachmentPath());
                messageHelper.addAttachment(Objects.requireNonNull(attachment.getFilename()), attachment);
            }

            javaMailSender.send(mimeMessage);

            System.out.println("Email sent successfully.");
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new CustomInternalServerException("Failed to send email: " + e.getMessage());
        }
    }


    @Override
    public void sendApplicationConfirmation(User user, String password, String regNo, String applicationNumber, School school) {
        try {
            EmailDetails emailDetails = new EmailDetails();
            emailDetails.setRecipient(user.getEmail());
            emailDetails.setSubject("Application Received - " + school.getSchoolName());

            // Prepare model for Thymeleaf template
            Map<String, Object> model = new HashMap<>();
            model.put("recipientName", user.getFirstName());
            model.put("password", password);
            model.put("regNo", regNo);
            model.put("applicationNumber", applicationNumber);
            model.put("schoolName", school.getSchoolName());
            model.put("contactEmail", school.getEmail());
            model.put("contactPhone", school.getPhoneNumber());
            model.put("currentYear", Year.now().getValue());
            model.put("submissionDate", LocalDate.now());

            emailDetails.setModel(model);
            emailDetails.setTemplateName("application-confirmation");

            sendEmailWithThymeleaf(emailDetails);
            logger.info("Application confirmation email sent to {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send application confirmation email", e);
            throw new CustomInternalServerException("Failed to send application confirmation email");
        }
    }

    @Override
    public void sendDocumentsRequest(String recipientEmail, String studentName, Set<String> missingDocuments, School school) {
        try {
            EmailDetails emailDetails = new EmailDetails();
            emailDetails.setRecipient(recipientEmail);
            emailDetails.setSubject("Additional Documents Required - " + school.getSchoolName());

            Map<String, Object> model = new HashMap<>();
            model.put("studentName", studentName);
            model.put("missingDocuments", missingDocuments);
            model.put("schoolName", school.getSchoolName());
            model.put("contactEmail", school.getEmail());
            model.put("submissionDeadline", LocalDate.now().plusDays(7).format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));

            emailDetails.setModel(model);
            emailDetails.setTemplateName("documents-request");

            sendEmailWithThymeleaf(emailDetails);
            logger.info("Documents request email sent to {}", recipientEmail);
        } catch (Exception e) {
            logger.error("Failed to send documents request email", e);
            throw new CustomInternalServerException("Failed to send documents request email");
        }
    }

    @Override
    public void sendPaymentRequest(String recipientEmail, String studentName, BigDecimal amount,
                                   String applicationNumber, School school) {
        try {

            EmailDetails emailDetails = new EmailDetails();
            emailDetails.setRecipient(recipientEmail);
            emailDetails.setSubject("Payment Request - " + school.getSchoolName());

            Map<String, Object> model = new HashMap<>();
            model.put("studentName", studentName);
            model.put("amount", amount);
            model.put("applicationNumber", applicationNumber);
            model.put("schoolName", school.getSchoolName());
            model.put("paymentDeadline", LocalDate.now().plusDays(14).format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
            model.put("contactEmail", school.getEmail());

            emailDetails.setModel(model);
            emailDetails.setTemplateName("payment-request");

            sendEmailWithThymeleaf(emailDetails);
            logger.info("Payment request email sent to {}", recipientEmail);
        } catch (Exception e) {
            logger.error("Failed to send payment request email", e);
            throw new CustomInternalServerException("Failed to send payment request email");
        }
    }

    @Override
    public void sendRejectionNotification(String recipientEmail, String studentName, String rejectionReason, School school) {
        try {
            EmailDetails emailDetails = new EmailDetails();
            emailDetails.setRecipient(recipientEmail);
            emailDetails.setSubject("Application Status Update - " + school.getSchoolName());

            Map<String, Object> model = new HashMap<>();
            model.put("studentName", studentName);
            model.put("rejectionReason", rejectionReason);
            model.put("schoolName", school.getSchoolName());
            model.put("contactEmail", school
                    .getEmail());
            model.put("contactPhone", school.getPhoneNumber());
            model.put("canReapply", true);
            model.put("reapplicationPeriod", "next academic session");

            emailDetails.setModel(model);
            emailDetails.setTemplateName("rejection-notification");

            sendEmailWithThymeleaf(emailDetails);
            logger.info("Rejection notification email sent to {}", recipientEmail);
        } catch (Exception e) {
            logger.error("Failed to send rejection notification email", e);
            throw new CustomInternalServerException("Failed to send rejection notification email");
        }
    }

    @Override
    public void sendPaymentVerificationRequest(AdmissionApplication application, School school) {
        try {
            EmailDetailsToMultipleEmails emailDetails = new EmailDetailsToMultipleEmails();
            emailDetails.setToEmails(Collections.singletonList(school.getEmail())); // Method to fetch admin emails
            emailDetails.setSubject("Payment Verification Required - Application #" + application.getApplicationNumber());

            Map<String, Object> model = new HashMap<>();
            model.put("applicationNumber", application.getApplicationNumber());
            model.put("studentName", application.getProfile().getUser().getFirstName() + " " + application.getProfile().getUser().getLastName());
            model.put("amountPaid", application.getApplicationFee());
            model.put("paymentReference", application.getPaymentReference());
            model.put("paymentDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a")));
            model.put("verificationLink", buildVerificationLink(application.getId()));

            emailDetails.setModel(model);
            emailDetails.setTemplateName("payment-verification-request");

            sendToMultipleEmails(emailDetails);
            logger.info("Payment verification request sent to admins");
        } catch (Exception e) {
            logger.error("Failed to send payment verification request", e);
            throw new CustomInternalServerException("Failed to send payment verification request");
        }
    }

    @Override
    public void sendAdmissionConfirmation(String recipientEmail, String studentName,
                                          String password, String registrationNumber, School school) {
        try {
            EmailDetails emailDetails = new EmailDetails();
            emailDetails.setRecipient(recipientEmail);
            emailDetails.setSubject("Admission Confirmation - " + school.getSchoolName());

            Map<String, Object> model = new HashMap<>();
            model.put("studentName", studentName);
            model.put("password", password);
            model.put("registrationNumber", registrationNumber);
            model.put("schoolName", school
                    .getSchoolName());
            model.put("contactEmail", school.getEmail());
            model.put("orientationDate", LocalDate.now().plusDays(21).format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));

            emailDetails.setModel(model);
            emailDetails.setTemplateName("admission-confirmation");

            sendEmailWithThymeleaf(emailDetails);
            logger.info("Admission confirmation email sent to {}", recipientEmail);
        } catch (Exception e) {
            logger.error("Failed to send admission confirmation email", e);
            throw new CustomInternalServerException("Failed to send admission confirmation email");
        }
    }


    @Override
    public void sendExamScheduleNotification(String recipientEmail, String studentName, LocalDateTime examDateTime,
                                             School school) {
        try {
            EmailDetails emailDetails = new EmailDetails();
            emailDetails.setRecipient(recipientEmail);
            emailDetails.setSubject("Entrance Exam Schedule - " + school.getSchoolName());

            Map<String, Object> model = new HashMap<>();
            model.put("studentName", studentName);
            model.put("examDateTime", examDateTime.format(DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a")));
            model.put("venue", school.getSchoolAddress());
            model.put("schoolName", school.getSchoolName());
            model.put("contactEmail", school.getEmail());
            model.put("contactPhone", school.getPhoneNumber());

            emailDetails.setModel(model);
            emailDetails.setTemplateName("admission-examination");

            sendEmailWithThymeleaf(emailDetails);
            logger.info("Exam schedule notification email sent to {}", recipientEmail);
        } catch (Exception e) {
            logger.error("Failed to send exam schedule notification email", e);
            throw new CustomInternalServerException("Failed to send exam schedule notification email");
        }
    }


    private String buildVerificationLink(Long applicationId) {
        return "${school.base.url}/api/v1/applications/" + applicationId + "/verify-payment";
    }

}

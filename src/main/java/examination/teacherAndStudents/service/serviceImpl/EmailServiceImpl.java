package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.EmailDetails;
import examination.teacherAndStudents.dto.EmailDetailsToMultipleEmails;
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

import java.nio.charset.StandardCharsets;

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
                messageHelper.addAttachment(attachment.getFilename(), attachment);
            }

            javaMailSender.send(mimeMessage);

            System.out.println("Email sent successfully.");
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new CustomInternalServerException("Failed to send email: " + e.getMessage());
        }
    }

}

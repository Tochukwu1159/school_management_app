package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.EmailDetails;
import examination.teacherAndStudents.dto.EmailDetailsToMultipleEmails;
import jakarta.mail.MessagingException;

public interface EmailService {
    void sendHtmlEmail(EmailDetails emailDetails) throws MessagingException;
     void sendEmailWithThymeleaf(EmailDetails emailDetails);
    void sendToMultipleEmails(EmailDetailsToMultipleEmails emailDetails) throws MessagingException;
    void sendEmails(EmailDetails emailDetails);
    void sendEmailWithAttachment(EmailDetails emailDetails);}


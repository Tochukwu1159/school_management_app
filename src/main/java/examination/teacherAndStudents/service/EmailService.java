package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.EmailDetails;
import examination.teacherAndStudents.dto.EmailDetailsToMultipleEmails;
import examination.teacherAndStudents.entity.AdmissionApplication;
import examination.teacherAndStudents.entity.School;
import jakarta.mail.MessagingException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public interface EmailService {
    void sendHtmlEmail(EmailDetails emailDetails) throws MessagingException;
     void sendEmailWithThymeleaf(EmailDetails emailDetails);
    void sendToMultipleEmails(EmailDetailsToMultipleEmails emailDetails) throws MessagingException;
    void sendEmails(EmailDetails emailDetails);
    void sendEmailWithAttachment(EmailDetails emailDetails);
    void sendApplicationConfirmation(String recipientEmail, String studentName, String applicationNumber, School school);
    void sendDocumentsRequest(String recipientEmail, String studentName, Set<String> missingDocuments, School school);
    void sendPaymentRequest(String recipientEmail, String studentName, BigDecimal amount,
                            String applicationNumber, School school);
    void sendRejectionNotification(String recipientEmail, String studentName, String rejectionReason, School school);
    void sendPaymentVerificationRequest(AdmissionApplication application, School school);
    void sendAdmissionConfirmation(String recipientEmail, String studentName,
                                   String password, String registrationNumber, School school);
    void sendExamScheduleNotification(String recipientEmail, String studentName, LocalDateTime examDateTime,
                                      School school);}

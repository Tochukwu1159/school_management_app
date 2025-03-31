package examination.teacherAndStudents.jobs;

import examination.teacherAndStudents.entity.AcademicSession;
import examination.teacherAndStudents.entity.School;
import examination.teacherAndStudents.repository.AcademicSessionRepository;
import examination.teacherAndStudents.repository.SchoolRepository;
import examination.teacherAndStudents.service.AcademicSessionService;
import examination.teacherAndStudents.service.SchoolService;
import examination.teacherAndStudents.service.serviceImpl.EmailTemplateService;
import examination.teacherAndStudents.utils.SessionPromotion;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ScheduleJobs {

    private final SchoolService schoolService;
    private final SchoolRepository schoolRepository;
    private final AcademicSessionRepository academicSessionRepository;
    private final AcademicSessionService academicSessionService;
    private final EmailTemplateService emailTemplateService;

    @Scheduled(cron = "0 0 0 * * ?") // Every day at midnight
    public void deactivateExpiredSubscriptions() {
        schoolService.deactivateExpiredSubscriptions();
    }



    @Scheduled(cron = "0 0 0 * * ?")  // Runs daily at midnight
    public void checkAndNotifyExpiredSubscriptions() throws MessagingException {
        LocalDateTime now = LocalDateTime.now();

        // Find all active schools whose subscription hasn't expired yet
        List<School> activeSchools = schoolRepository.findByIsActiveTrueAndSubscriptionExpiryDateAfter(now);

        for (School school : activeSchools) {
            long daysRemaining = ChronoUnit.DAYS.between(now, school.getSubscriptionExpiryDate());

            // Only send notifications for specific days before expiration
            if (daysRemaining == 5 || daysRemaining == 3 || daysRemaining == 1) {
                emailTemplateService.sendExpiryReminder(school, (int) daysRemaining);
            }

            // Optional: Handle expiration on the day itself
            if (daysRemaining == 0) {
                emailTemplateService.sendSubscriptionExpiredNotification(school);
                // You might want to deactivate the school here
                school.setIsActive(false);
                schoolRepository.save(school);
            }
        }

    }
}

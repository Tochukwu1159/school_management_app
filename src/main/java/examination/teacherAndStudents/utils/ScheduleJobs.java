package examination.teacherAndStudents.utils;

import examination.teacherAndStudents.entity.AcademicSession;
import examination.teacherAndStudents.entity.School;
import examination.teacherAndStudents.repository.AcademicSessionRepository;
import examination.teacherAndStudents.repository.SchoolRepository;
import examination.teacherAndStudents.service.AcademicSessionService;
import examination.teacherAndStudents.service.SchoolService;
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

    @Scheduled(cron = "0 0 0 * * ?") // Every day at midnight
    public void deactivateExpiredSubscriptions() {
        schoolService.deactivateExpiredSubscriptions();
    }


    @Scheduled(cron = "0 0 0 1 * ?")
    public void checkSessionsAndGraduateStudents() {
        // Fetch all academic sessions that are active and have ended
        List<AcademicSession> endedSessions = academicSessionRepository.findByEndDateBeforeAndStatus(LocalDate.now(), AcademicSession.Status.OPEN);

        for (AcademicSession session : endedSessions) {
            try {
                // Call the graduation method for the session
                academicSessionService.graduateStudentsForSession(session.getId());

                // Optionally update the session status to CLOSED
                session.setStatus(AcademicSession.Status.CLOSED);
                academicSessionRepository.save(session);

                System.out.println("Graduation process completed for session: " + session.getName());
            } catch (Exception e) {
                System.err.println("Error processing graduation for session " + session.getName() + ": " + e.getMessage());
            }
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")  // Runs daily at midnight
    public void checkAndNotifyExpiredSubscriptions() {
        LocalDateTime now = LocalDateTime.now();

        // Find all active schools whose subscription is expiring in the next 5, 3, or 1 days
        List<School> expiringSchools = schoolRepository.findBySubscriptionExpiryDateBetweenAndIsActiveTrue(
                now.plusDays(1), now.plusDays(5));

//        for (School school : expiringSchools) {
//            long daysRemaining = ChronoUnit.DAYS.between(now, school.getSubscriptionExpiryDate());
//
//            if (daysRemaining == 5) {
//                // Send a reminder for 5 days before expiration
//                notificationService.sendExpiryReminder(school, 5);
//            } else if (daysRemaining == 3) {
//                // Send a reminder for 3 days before expiration
//                notificationService.sendExpiryReminder(school, 3);
//            } else if (daysRemaining == 1) {
//                // Send a reminder for 1 day before expiration
//                notificationService.sendExpiryReminder(school, 1);
//            }
//        }
    }


}

package examination.teacherAndStudents.utils;

import examination.teacherAndStudents.entity.School;
import examination.teacherAndStudents.repository.SchoolRepository;
import examination.teacherAndStudents.service.SchoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ScheduleJobs {

    private final SchoolService schoolService;
    private final SchoolRepository schoolRepository;

    @Scheduled(cron = "0 0 0 * * ?") // Every day at midnight
    public void deactivateExpiredSubscriptions() {
        schoolService.deactivateExpiredSubscriptions();
    }

//    @Scheduled(cron = "0 0 0 * * ?")  // Runs daily at midnight
//    public void checkAndNotifyExpiredSubscriptions() {
//        List<School> expiringSchools = schoolRepository.findExpiringSubscriptions(LocalDate.now());
//        for (School school : expiringSchools) {
//            notificationService.sendExpiryReminder(school);
//        }
//    }

}

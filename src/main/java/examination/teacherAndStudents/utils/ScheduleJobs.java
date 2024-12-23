package examination.teacherAndStudents.utils;

import examination.teacherAndStudents.service.SchoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduleJobs {

    private final SchoolService schoolService;

    @Scheduled(cron = "0 0 0 * * ?") // Every day at midnight
    public void deactivateExpiredSubscriptions() {
        schoolService.deactivateExpiredSubscriptions();
    }
}

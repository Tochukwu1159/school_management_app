package examination.teacherAndStudents.jobs;

import examination.teacherAndStudents.entity.AcademicSession;
import examination.teacherAndStudents.entity.StudentTerm;
import examination.teacherAndStudents.repository.AcademicSessionRepository;
import examination.teacherAndStudents.repository.StudentTermRepository;
import examination.teacherAndStudents.service.PositionService;
import examination.teacherAndStudents.service.ResultService;
import examination.teacherAndStudents.utils.SessionStatus;
import examination.teacherAndStudents.utils.TermStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AcademicScheduler {

    private final StudentTermRepository studentTermRepository;
    private final AcademicSessionRepository academicSessionRepository;
    private final ResultService resultService;
    private final PositionService positionService;




    // Runs every day at midnight to check if term resultReadyDate is reached
    @Scheduled(cron = "0 0 0 * * ?")
//    @Scheduled(cron = "0 */2 * * * ?")
    @Transactional
    public void updatePositionsForClass() {
        List<StudentTerm> terms = studentTermRepository.findByResultReadyDateAndTermStatus(LocalDate.now(), TermStatus.ACTIVE);
        for (StudentTerm term : terms) {
            resultService.calculateAverageResultJob(term.getAcademicSession().getId(), term.getId());
            positionService.updatePositionsForSessionForJob(term.getAcademicSession().getId(), term.getId());
            term.setTermStatus(TermStatus.COMPLETED); // Mark as completed
            studentTermRepository.save(term);

//            positionService.generateReportCardSummaryJob(term.getAcademicSession().getId());
        }
    }

    // Runs every day at midnight to check if session resultReadyDate is reached
    @Scheduled(cron = "0 0 0 * * ?")
//    @Scheduled(cron = "0 */2 * * * ?")
    @Transactional
    public void updateSessionAverageAndTop5Students() {
        AcademicSession session = academicSessionRepository.findByResultReadyDateBeforeOrEqualAndStatus(LocalDate.now(),
                SessionStatus.ACTIVE);
            resultService.updateSessionAverageForJob(session);
            resultService.getTop5StudentsForAllClasses(session);
            session.setStatus(
                    SessionStatus.CLOSED);
            academicSessionRepository.save(session);

    }
}



package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.BulkExamScheduleRequest;
import examination.teacherAndStudents.dto.ExamScheduleRequest;
import examination.teacherAndStudents.dto.ExamScheduleResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.ExamScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamScheduleServiceImpl implements ExamScheduleService {

    private final ExamScheduleRepository examScheduleRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final ClassBlockRepository classBlockRepository;
    private final AcademicSessionRepository academicSessionRepository;
    private final StudentTermRepository studentTermRepository;
    private final SessionClassRepository sessionClassRepository;

    @Override
    @Transactional
    public ExamScheduleResponse createExamSchedule(ExamScheduleRequest request) {
        // Since createExamSchedule is for single schedule, termId, yearId, and classBlockId must be provided externally
        throw new UnsupportedOperationException("Single schedule creation requires termId, yearId, and classBlockId. Use bulk creation for full payload.");
    }

    @Override
    @Transactional
    public List<ExamScheduleResponse> createBulkExamSchedules(BulkExamScheduleRequest request) {
        // Validate shared fields
        if (request.getTermId() == null || request.getYearId() == null || request.getClassBlockId() == null) {
            throw new CustomInternalServerException("Term ID, Year ID, and Class Block ID are required");
        }
        if (request.getSubjectSchedules() == null || request.getSubjectSchedules().isEmpty()) {
            throw new CustomInternalServerException("At least one subject schedule is required");
        }

        // Fetch shared entities
           classBlockRepository.findById(request.getClassBlockId())
                .orElseThrow(() -> new CustomNotFoundException("Class block not found with ID: " + request.getClassBlockId()));
        AcademicSession session = academicSessionRepository.findById(request.getYearId())
                .orElseThrow(() -> new CustomNotFoundException("Academic session not found with ID: " + request.getYearId()));
        StudentTerm term = studentTermRepository.findById(request.getTermId())
                .orElseThrow(() -> new CustomNotFoundException("Term not found with ID: " + request.getTermId()));

        SessionClass sessionClass = sessionClassRepository.findBySessionIdAndClassBlockId(request.getYearId(), request.getClassBlockId())
                .orElseThrow(() -> new CustomNotFoundException("SessionClass not found for Academic Session "));

        // Process each schedule
        List<ExamSchedule> schedules = request.getSubjectSchedules().stream()
                .map(scheduleRequest -> {
                    // Validate individual schedule
                    validateRequest(scheduleRequest, sessionClass.getId());

                    // Build ExamSchedule entity
                    Subject subject = subjectRepository.findById(scheduleRequest.getSubjectId())
                            .orElseThrow(() -> new CustomNotFoundException("Subject not found with ID: " ));
                    User teacher = userRepository.findById(scheduleRequest.getTeacherId())
                            .orElseThrow(() -> new CustomNotFoundException("Teacher not found with ID: " + scheduleRequest.getTeacherId()));

                    return ExamSchedule.builder()
                            .subject(subject)
                            .teacher(teacher)
                            .sessionClass(sessionClass)
                            .academicSession(session)
                            .studentTerm(term)
                            .examDate(scheduleRequest.getExamDate())
                            .startTime(scheduleRequest.getStartTime())
                            .endTime(scheduleRequest.getEndTime())
                            .build();
                })
                .collect(Collectors.toList());

        // Save all schedules
        examScheduleRepository.saveAll(schedules);

        // Map to responses
        return schedules.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ExamScheduleResponse updateExamSchedule(Long id, ExamScheduleRequest request) {
        validateRequest(request, null);
        ExamSchedule schedule = examScheduleRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Exam schedule not found with ID: " + id));
        updateScheduleFromRequest(schedule, request);
        examScheduleRepository.save(schedule);
        return mapToResponse(schedule);
    }

    @Override
    @Transactional
    public void deleteExamSchedule(Long id) {
        ExamSchedule schedule = examScheduleRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Exam schedule not found with ID: " + id));
        examScheduleRepository.delete(schedule);
    }

    @Override
    public Page<ExamScheduleResponse> getAllExamSchedules(Long subjectId, Long teacherId, LocalDate examDate, Pageable pageable) {
        return examScheduleRepository.findAllWithFilters(subjectId, teacherId, examDate, pageable)
                .map(this::mapToResponse);
    }


    private void validateRequest(ExamScheduleRequest request, Long classSessionId) {
        if (request.getStartTime().isAfter(request.getEndTime()) || request.getStartTime().equals(request.getEndTime())) {
            throw new CustomInternalServerException("Start time must be before end time");
        }
        if (classSessionId != null) {
            boolean hasOverlap = examScheduleRepository.existsOverlappingSchedule(
                    classSessionId, request.getExamDate(), request.getStartTime(), request.getEndTime());
            if (hasOverlap) {
                throw new CustomInternalServerException("Schedule overlaps with an existing schedule for the same session class and date");
            }
        }
    }

    private void updateScheduleFromRequest(ExamSchedule schedule, ExamScheduleRequest request) {
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new CustomNotFoundException("Subject not found with ID: " + request.getSubjectId()));
        User teacher = userRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new CustomNotFoundException("Teacher not found with ID: " + request.getTeacherId()));
        // termId, yearId, and classBlockId are assumed to remain unchanged for updates
        schedule.setSubject(subject);
        schedule.setTeacher(teacher);
        schedule.setExamDate(request.getExamDate());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
    }

    private ExamScheduleResponse mapToResponse(ExamSchedule schedule) {
        ExamScheduleResponse response = new ExamScheduleResponse();
        response.setId(schedule.getId());
        response.setSubjectId(schedule.getSubject().getId());
        response.setSubjectName(schedule.getSubject().getName());
        response.setTeacherId(schedule.getTeacher().getId());
        response.setTeacherName(schedule.getTeacher().getFirstName() + " " + schedule.getTeacher().getLastName());
        response.setSessionClassId(schedule.getSessionClass().getId());
        response.setSessionClassName(schedule.getSessionClass().getClassBlock().getName() + " (" +
                schedule.getSessionClass().getAcademicSession().getSessionName() + ")");
        response.setTermId(schedule.getStudentTerm().getId());
        response.setTermName(schedule.getStudentTerm().getName());
        response.setYearId(schedule.getSessionClass().getAcademicSession().getId());
        response.setYearName(schedule.getSessionClass().getAcademicSession().getSessionName().getName());
        response.setExamDate(schedule.getExamDate());
        response.setStartTime(schedule.getStartTime());
        response.setEndTime(schedule.getEndTime());
        return response;
    }
}
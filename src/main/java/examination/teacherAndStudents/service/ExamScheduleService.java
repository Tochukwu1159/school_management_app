package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.BulkExamScheduleRequest;
import examination.teacherAndStudents.dto.ExamScheduleRequest;
import examination.teacherAndStudents.dto.ExamScheduleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ExamScheduleService {
    ExamScheduleResponse createExamSchedule(ExamScheduleRequest request);
    List<ExamScheduleResponse> createBulkExamSchedules(BulkExamScheduleRequest request);
    ExamScheduleResponse updateExamSchedule(Long id, ExamScheduleRequest request);
    void deleteExamSchedule(Long id);
    Page<ExamScheduleResponse> getAllExamSchedules(Long subjectId, Long teacherId, LocalDate examDate, Pageable pageable);
}

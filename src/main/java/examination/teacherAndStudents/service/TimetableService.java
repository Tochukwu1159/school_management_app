package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.SubjectScheduleRequest;
import examination.teacherAndStudents.entity.ClassLevel;
import examination.teacherAndStudents.entity.SubjectSchedule;
import examination.teacherAndStudents.entity.Timetable;
import examination.teacherAndStudents.repository.TimetableRepository;
import examination.teacherAndStudents.utils.DayOfWeek;
import examination.teacherAndStudents.utils.StudentTerm;
import examination.teacherAndStudents.utils.TimetableType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public interface TimetableService {

    Timetable createTimetable( DayOfWeek dayOfWeek, List<SubjectScheduleRequest> subjectSchedules, Long termId, Long sessionId, Long classBlockId);
    Timetable updateTimetable(Long timetableId, DayOfWeek dayOfWeek, List<SubjectScheduleRequest> subjectSchedules, Long termId, Long sessionId);
    Page<Timetable> getAllTimetables(int page, int size, String sortBy, String sortDirection);
    Page<SubjectSchedule> getAllSubjectSchedules(int page, int size, String sortBy, String sortDirection);
    Timetable getTimetableById(Long timetableId);
    void deleteTimetable(Long timetableId);}
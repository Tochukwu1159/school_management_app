package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.TimetableCreationRequest;
import examination.teacherAndStudents.entity.SubjectSchedule;
import examination.teacherAndStudents.entity.Timetable;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.TimetableRepository;
import examination.teacherAndStudents.service.SubjectService;
import examination.teacherAndStudents.service.TimetableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/timetable")
public class TimetableController {
    @Autowired
    private TimetableService timetableService;
    @Autowired
    private TimetableRepository timetableRepository;

    @PostMapping("/create")
    public ResponseEntity<String> createTimetable(@RequestBody TimetableCreationRequest request) {
        Timetable timetable = timetableService.createTimetable(
                request.getDayOfWeek(),
                request.getSubjectSchedules(),
                request.getTimetableType(),
                request.getTerm(),
                request.getYearId(),
                request.getClassBlockId()
        );

        return ResponseEntity.ok("Timetable created with ID: " + timetable.getId());
    }

    @PutMapping("/update/{timetableId}")
    public ResponseEntity<String> updateTimetable(@PathVariable Long timetableId, @RequestBody TimetableCreationRequest request) {
        Timetable updatedTimetable = timetableService.updateTimetable(
                timetableId,
                request.getDayOfWeek(),
                request.getSubjectSchedules(),
                request.getTerm(),
                request.getYearId()
        );
        return ResponseEntity.ok("Timetable created with ID: " + timetableId);

    }
    @GetMapping("/{timetableId}")
    public ResponseEntity<Timetable> getTimetableById(@PathVariable Long timetableId){
        Timetable timeTable =   timetableService.getTimetableById(timetableId);
        return new ResponseEntity<>(timeTable, HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<Page<Timetable>> getAllTimetables(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        Page<Timetable> timetables = timetableService.getAllTimetables(page, size, sortBy, sortDirection);
        return new ResponseEntity<>(timetables, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Page<SubjectSchedule>> getSubjectSchedules(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        Page<SubjectSchedule> subjectSchedules = timetableService.getAllSubjectSchedules(page, size, sortBy, sortDirection);
        return new ResponseEntity<>(subjectSchedules, HttpStatus.OK);
    }



    public void deleteTimetable(Long timetableId) {
        if (!timetableRepository.existsById(timetableId)) {
            throw new CustomNotFoundException("Timetable not found with ID: " + timetableId);
        }

        timetableService.deleteTimetable(timetableId);
    }
}

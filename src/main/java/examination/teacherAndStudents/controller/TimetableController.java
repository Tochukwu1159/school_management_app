package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.TimetableCreationRequest;
import examination.teacherAndStudents.entity.SubjectSchedule;
import examination.teacherAndStudents.entity.Timetable;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.TimetableRepository;
import examination.teacherAndStudents.service.SubjectService;
import examination.teacherAndStudents.service.TimetableService;
import org.springframework.beans.factory.annotation.Autowired;
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
                request.getSchoolClassId(),
                request.getTeacherId(),
                request.getDayOfWeek(),
                request.getSubjectSchedules(),
                request.getTimetableType(),
                request.getTerm(),
                request.getYearId()
        );

        return ResponseEntity.ok("Timetable created with ID: " + timetable.getId());
    }

    @PutMapping("/update/{timetableId}")
    public ResponseEntity<String> updateTimetable(@PathVariable Long timetableId, @RequestBody TimetableCreationRequest request) {
        Timetable updatedTimetable = timetableService.updateTimetable(
                timetableId,
                request.getSchoolClassId(),
                request.getTeacherId(),
                request.getDayOfWeek(),
                request.getSubjectSchedules(),
                request.getTerm(),
                request.getYearId()
        );
        return ResponseEntity.ok("Timetable created with ID: " + timetableId);

    }
    @GetMapping("/{timetableId}")
    public ResponseEntity<Timetable> getTimetableById(@PathVariable Long timetableId){
        return timetableService.getTimetableById(timetableId);
    }

    @GetMapping
    public ResponseEntity<List<Timetable>> getAllTimetables() {
        List<Timetable> timetables = timetableService.getAllTimetables();
        return new ResponseEntity<>(timetables, HttpStatus.OK);
    }
    public void deleteTimetable(Long timetableId) {
        if (!timetableRepository.existsById(timetableId)) {
            throw new CustomNotFoundException("Timetable not found with ID: " + timetableId);
        }

        timetableService.deleteTimetable(timetableId);
    }




    // Other API endpoints as needed
}

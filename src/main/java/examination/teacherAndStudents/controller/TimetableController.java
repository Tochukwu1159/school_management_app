package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.TimetableCreationRequest;
import examination.teacherAndStudents.entity.SubjectSchedule;
import examination.teacherAndStudents.entity.Timetable;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.TimetableRepository;
import examination.teacherAndStudents.service.TimetableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/timetable")
public class TimetableController {

    private final TimetableService timetableService;
    private final TimetableRepository timetableRepository;

    @Autowired
    public TimetableController(TimetableService timetableService, TimetableRepository timetableRepository) {
        this.timetableService = timetableService;
        this.timetableRepository = timetableRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<String>> createTimetable(@RequestBody TimetableCreationRequest request) {
        Timetable timetable = timetableService.createTimetable(
                request.getDayOfWeek(),
                request.getSubjectSchedules(),
                request.getTerm(),
                request.getYearId(),
                request.getClassBlockId()
        );

        ApiResponse<String> response = new ApiResponse<>("Timetable created successfully", true, "Timetable created with ID: " + timetable.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{timetableId}")
    public ResponseEntity<ApiResponse<String>> updateTimetable(@PathVariable Long timetableId, @RequestBody TimetableCreationRequest request) {
        Timetable updatedTimetable = timetableService.updateTimetable(
                timetableId,
                request.getDayOfWeek(),
                request.getSubjectSchedules(),
                request.getTerm(),
                request.getYearId()
        );

        ApiResponse<String> response = new ApiResponse<>("Timetable updated successfully", true, "Timetable updated with ID: " + updatedTimetable.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{timetableId}")
    public ResponseEntity<ApiResponse<Timetable>> getTimetableById(@PathVariable Long timetableId) {
        Timetable timetable = timetableService.getTimetableById(timetableId);

        ApiResponse<Timetable> response = new ApiResponse<>("Timetable fetched successfully", true, timetable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Page<Timetable>>> getAllTimetables(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        Page<Timetable> timetables = timetableService.getAllTimetables(page, size, sortBy, sortDirection);

        ApiResponse<Page<Timetable>> response = new ApiResponse<>("Timetables fetched successfully", true, timetables);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/subjects")
    public ResponseEntity<ApiResponse<Page<SubjectSchedule>>> getSubjectSchedules(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        Page<SubjectSchedule> subjectSchedules = timetableService.getAllSubjectSchedules(page, size, sortBy, sortDirection);

        ApiResponse<Page<SubjectSchedule>> response = new ApiResponse<>("Subject schedules fetched successfully", true, subjectSchedules);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{timetableId}")
    public ResponseEntity<ApiResponse<Void>> deleteTimetable(@PathVariable Long timetableId) {
        if (!timetableRepository.existsById(timetableId)) {
            throw new CustomNotFoundException("Timetable not found with ID: " + timetableId);
        }

        timetableService.deleteTimetable(timetableId);
        ApiResponse<Void> response = new ApiResponse<>("Timetable deleted successfully", true, null);
        return ResponseEntity.ok(response);
    }
}

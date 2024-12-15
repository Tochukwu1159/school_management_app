package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.VisitorsRequest;
import examination.teacherAndStudents.dto.VisitorsResponse;
import examination.teacherAndStudents.service.VisitorsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/visitors")
public class VisitorsController {

    private final VisitorsService visitorsService;

    @Autowired
    public VisitorsController(VisitorsService visitorsService) {
        this.visitorsService = visitorsService;
    }

    @PostMapping
    public ResponseEntity<VisitorsResponse> addVisitor(@RequestBody VisitorsRequest request) {
        VisitorsResponse response = visitorsService.addVisitor(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VisitorsResponse> editVisitor(@PathVariable Long id, @RequestBody VisitorsRequest request) {
        VisitorsResponse response = visitorsService.editVisitor(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVisitor(@PathVariable Long id) {
        visitorsService.deleteVisitor(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<VisitorsResponse>> getAllVisitors(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy) {
        Page<VisitorsResponse> visitorsPage = visitorsService.getAllVisitors(pageNo, pageSize, sortBy);
        return ResponseEntity.ok(visitorsPage);
    }
}

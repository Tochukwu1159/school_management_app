package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.SchoolRequest;
import examination.teacherAndStudents.dto.SchoolResponse;
import examination.teacherAndStudents.service.SchoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/schools")
public class SchoolController {

    @Autowired
    private SchoolService schoolService;

    @PostMapping("/onboard")
    public ResponseEntity<SchoolResponse> onboardSchool(@RequestBody SchoolRequest school) {
        SchoolResponse onboardedSchool = schoolService.onboardSchool(school );
        return new ResponseEntity<>(onboardedSchool, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/services")
    public ResponseEntity<List<String>> getSelectedServices(@PathVariable Long id) {
        List<String> selectedServices = schoolService.getSelectedServices(id);
        return ResponseEntity.ok(selectedServices);
    }

    // Other endpoints for managing schools
}

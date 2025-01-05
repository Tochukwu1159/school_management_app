package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.entity.School;
import examination.teacherAndStudents.entity.ServiceOffered;
import examination.teacherAndStudents.service.SchoolService;
import examination.teacherAndStudents.utils.ServiceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/schools")
public class SchoolController {

    @Autowired
    private SchoolService schoolService;

    @PostMapping("/onboard")
    public ResponseEntity<SchoolResponse> onboardSchool(@RequestBody SchoolRequest school) {
        SchoolResponse onboardedSchool = schoolService.onboardSchool(school );
        return new ResponseEntity<>(onboardedSchool, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<SchoolLoginResponse>   loginSchool(@RequestBody LoginRequest school) {
        SchoolLoginResponse loginSchool = schoolService.loginSchool(school );
        return new ResponseEntity<>(loginSchool, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/services")
    public ResponseEntity<List<ServiceOffered>> getSelectedServices(@PathVariable Long id) {
        List<ServiceOffered> selectedServices = schoolService.getSelectedServices(id);
        return ResponseEntity.ok(selectedServices);
    }

    @PutMapping("/{id}/subscribe")
    public ResponseEntity<School> subscribeSchool(
            @PathVariable Long id,
            @RequestBody SubscriptionRequest subscriptionRequest) throws Exception {
        return ResponseEntity.ok(schoolService.subscribeSchool(id, subscriptionRequest));
    }


    @GetMapping("/{id}/validate-subscription")
    public ResponseEntity<Boolean> validateSubscription(@PathVariable Long id) {
        return ResponseEntity.ok(schoolService.isValidSubscriptionKey(id));
    }

    @PostMapping("/deactivate-expired")
    public ResponseEntity<String> deactivateExpiredSubscriptions() {
        schoolService.deactivateExpiredSubscriptions();
        return ResponseEntity.ok("Expired subscriptions deactivated successfully.");
    }

    // Other endpoints for managing schools
}

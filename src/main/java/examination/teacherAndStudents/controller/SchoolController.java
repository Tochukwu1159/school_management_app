package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.entity.School;
import examination.teacherAndStudents.entity.ServiceOffered;
import examination.teacherAndStudents.service.SchoolService;
import examination.teacherAndStudents.utils.ServiceType;
import examination.teacherAndStudents.utils.SubscriptionType;
import jakarta.validation.Valid;
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

    @GetMapping("/{id}/services")
    public ResponseEntity<List<ServiceOffered>> getSelectedServices(@PathVariable Long id) {
        List<ServiceOffered> selectedServices = schoolService.getSelectedServices(id);
        return ResponseEntity.ok(selectedServices);
    }

    @PutMapping("/subscribe")
    public ResponseEntity<School> subscribeSchool(
            @RequestBody SubscriptionRequest subscriptionRequest) throws Exception {
        return ResponseEntity.ok(schoolService.subscribeSchool(subscriptionRequest));
    }

    @PutMapping("/renew-subscribe")
    public ResponseEntity<School> renewSubscribeSchool(
            @RequestBody SubscriptionType subscriptionRequest) throws Exception {
        return ResponseEntity.ok(schoolService.renewSubscription(subscriptionRequest));
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

    @GetMapping
    public ResponseEntity<List<School>> getAllSchools() {
        return ResponseEntity.ok(schoolService.getAllSchools());
    }

    @GetMapping("/{id}")
    public ResponseEntity<School> getSchoolById(@PathVariable Long id) {
        return ResponseEntity.ok(schoolService.getSchoolById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<School> updateSchool(@PathVariable Long id, @Valid @RequestBody SchoolRequest request) {
        return ResponseEntity.ok(schoolService.updateSchool(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchool(@PathVariable Long id) {
        schoolService.deleteSchool(id);
        return ResponseEntity.noContent().build();
    }

    // Other endpoints for managing schools
}

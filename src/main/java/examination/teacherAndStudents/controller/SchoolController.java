package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.SchoolRequest;
import examination.teacherAndStudents.dto.SchoolResponse;
import examination.teacherAndStudents.entity.School;
import examination.teacherAndStudents.service.SchoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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

    @PutMapping("/{id}/subscribe")
    public ResponseEntity<School> subscribeSchool(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newExpiryDate) {
        return ResponseEntity.ok(schoolService.subscribeSchool(id, newExpiryDate));
    }

//    @PutMapping("/{id}/renew-subscription")
//    public ResponseEntity<School> renewSubscription(
//            @PathVariable Long id,
//            @RequestParam int additionalDays) {
//        return ResponseEntity.ok(schoolService.renewSubscription(id, additionalDays));
//    }

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

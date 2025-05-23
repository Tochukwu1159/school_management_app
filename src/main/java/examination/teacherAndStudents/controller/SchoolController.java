// SchoolController.java
package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.entity.School;
import examination.teacherAndStudents.entity.ServiceOffered;
import examination.teacherAndStudents.service.SchoolService;
import examination.teacherAndStudents.utils.SubscriptionType;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/schools")
public class SchoolController {

    @Autowired
    private SchoolService schoolService;

    @PostMapping("/onboard")
    public ResponseEntity<ApiResponse<SchoolResponse>> onboardSchool(@Valid @RequestBody SchoolRequest schoolRequest) {
        SchoolResponse onboardedSchool = schoolService.onboardSchool(schoolRequest);
        ApiResponse<SchoolResponse> response = new ApiResponse<>("School onboarded successfully", true, onboardedSchool);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/services")
    public ResponseEntity<ApiResponse<List<ServiceOffered>>> getSelectedServices(@PathVariable Long id) {
        List<ServiceOffered> selectedServices = schoolService.getSelectedServices(id);
        ApiResponse<List<ServiceOffered>> response = new ApiResponse<>("Selected services retrieved successfully", true, selectedServices);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/subscribe")
    public ResponseEntity<ApiResponse<School>> subscribeSchool(
            @Valid @RequestBody SubscriptionRequest subscriptionRequest) throws Exception {
        School school = schoolService.subscribeSchool(subscriptionRequest);
        ApiResponse<School> response = new ApiResponse<>("School subscribed successfully", true, school);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/renew-subscribe")
    public ResponseEntity<ApiResponse<School>> renewSubscribeSchool(
            @Valid @RequestBody SubscriptionType subscriptionType) throws Exception {
        School school = schoolService.renewSubscription(subscriptionType);
        ApiResponse<School> response = new ApiResponse<>("Subscription renewed successfully", true, school);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/validate-subscription")
    public ResponseEntity<ApiResponse<Boolean>> validateSubscription(@PathVariable Long id) {
        boolean isValid = schoolService.isValidSubscriptionKey(id);
        ApiResponse<Boolean> response = new ApiResponse<>("Subscription validation result", true, isValid);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<WalletResponse>> walletBalance() {
        ApiResponse<WalletResponse> response = new ApiResponse<>("School  wallet retrieved successfully", true, schoolService.walletBalance());
        return ResponseEntity.ok(response);    }

    
    @PostMapping("/deactivate-expired")
    public ResponseEntity<ApiResponse<String>> deactivateExpiredSubscriptions() {
        schoolService.deactivateExpiredSubscriptions();
        ApiResponse<String> response = new ApiResponse<>("Expired subscriptions deactivated successfully", true, "Success");
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<School>>> getAllSchools() {
        List<School> schools = schoolService.getAllSchools();
        ApiResponse<List<School>> response = new ApiResponse<>("Schools retrieved successfully", true, schools);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<School>> getSchoolById(@PathVariable Long id) {
        School school = schoolService.getSchoolById(id);
        ApiResponse<School> response = new ApiResponse<>("School retrieved successfully", true, school);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<School>> updateSchool(@PathVariable Long id, @Valid @RequestBody SchoolRequest request) {
        School updatedSchool = schoolService.updateSchool(id, request);
        ApiResponse<School> response = new ApiResponse<>("School updated successfully", true, updatedSchool);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteSchool(@PathVariable Long id) {
        schoolService.deleteSchool(id);
        ApiResponse<String> response = new ApiResponse<>("School deleted successfully", true, "Deleted");
        return ResponseEntity.ok(response);
    }
}

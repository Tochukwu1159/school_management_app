package examination.teacherAndStudents.controller;
import examination.teacherAndStudents.dto.StaffRequest;
import examination.teacherAndStudents.dto.StaffResponse;
import examination.teacherAndStudents.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/-staff")
public class StaffController {


    private StaffService staffService;

    @PostMapping("/create")
    public ResponseEntity<StaffResponse> createStaff(@RequestBody StaffRequest staffRequest) {
        StaffResponse createdStaff = staffService.createStaff(staffRequest);
        return new ResponseEntity<>(createdStaff, HttpStatus.CREATED);
    }

    @PutMapping("/update/{StaffId}")
    public ResponseEntity<StaffResponse> updateStaff(
            @PathVariable Long staffId,
            @RequestBody StaffRequest updatedStaff) {
        StaffResponse updatedStaffResponse = staffService.updateStaff(staffId, updatedStaff);
        return new ResponseEntity<>(updatedStaffResponse, HttpStatus.OK);
    }

    @GetMapping("/findAll")
    public ResponseEntity<Page<StaffResponse>> findAllStaff(String searchTerm, int page, int size, String sortBy){
        Page<StaffResponse> allStaff = staffService.findAllStaff(searchTerm,page, size, sortBy);
        return new ResponseEntity<>(allStaff, HttpStatus.OK);
    }

    @GetMapping("/findById/{StaffId}")
    public ResponseEntity<StaffResponse> findStaffById(@PathVariable Long StaffId) {
        StaffResponse staffById = staffService.findStaffById(StaffId);
        return new ResponseEntity<>(staffById, HttpStatus.OK);
    }

    @PostMapping("/deactivate/{uniqueRegistrationNumber}")
    public StaffResponse deactivateStudent(@PathVariable String uniqueRegistrationNumber){
        return staffService.deactivateStaff(uniqueRegistrationNumber);

    }

    @DeleteMapping("/delete/{StaffId}")
    public ResponseEntity<Void> deleteStaff(@PathVariable Long StaffId) {
        staffService.deleteStaff(StaffId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

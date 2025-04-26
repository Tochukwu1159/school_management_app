package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.HostelRequest;
import examination.teacherAndStudents.dto.HostelResponse;
import examination.teacherAndStudents.service.HostelService;
import examination.teacherAndStudents.utils.AvailabilityStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/hostels")
@RequiredArgsConstructor
public class HostelController {

    private final HostelService hostelService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<HostelResponse>>> getAllHostels(
            @RequestParam(required = false) String hostelName,
            @RequestParam(required = false) AvailabilityStatus availabilityStatus,
            @RequestParam(required = false) Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "hostelName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        Page<HostelResponse> hostelsPage = hostelService.getAllHostels(
                hostelName,
                availabilityStatus,
                id,
                page,
                size,
                sortBy,
                sortDirection);

        ApiResponse<Page<HostelResponse>> apiResponse = new ApiResponse<>("Hostels fetched successfully", true, hostelsPage);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HostelResponse>> getHostelById(@PathVariable Long id) {
        HostelResponse hostel = hostelService.getHostelById(id);
        ApiResponse<HostelResponse> apiResponse = new ApiResponse<>("Hostel fetched successfully", true, hostel);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<HostelResponse>> createHostel(@RequestBody HostelRequest hostelRequest) {
        HostelResponse createdHostel = hostelService.createHostel(hostelRequest);
        ApiResponse<HostelResponse> apiResponse = new ApiResponse<>("Hostel created successfully", true, createdHostel);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HostelResponse>> updateHostel(
            @PathVariable Long id,
            @RequestBody HostelRequest updatedHostel) {
        HostelResponse hostel = hostelService.updateHostel(id, updatedHostel);
        ApiResponse<HostelResponse> apiResponse = new ApiResponse<>("Hostel updated successfully", true, hostel);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteHostel(@PathVariable Long id) {
        hostelService.deleteHostel(id);
        ApiResponse<Void> apiResponse = new ApiResponse<>("Hostel deleted successfully", true, null);
        return ResponseEntity.ok(apiResponse);
    }
}

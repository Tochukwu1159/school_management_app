package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.HostelAllocationRequest;
import examination.teacherAndStudents.dto.HostelAllocationResponse;
import examination.teacherAndStudents.service.HostelAllocationService;
import examination.teacherAndStudents.utils.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/hostel-allocations")
@RequiredArgsConstructor
public class HostelAllocationController {

    private final HostelAllocationService hostelAllocationService;

    @PostMapping("/pay")
    public ResponseEntity<ApiResponse<HostelAllocationResponse>> payHostelAllocation(
            @RequestParam Long dueId,
            @RequestParam Long sessionId
    ) {
        HostelAllocationResponse response = hostelAllocationService.payHotelAllocation(dueId, sessionId);
        ApiResponse<HostelAllocationResponse> apiResponse = new ApiResponse<>("Hostel allocation payment successful", true, response);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/allocate")
    public ResponseEntity<ApiResponse<HostelAllocationResponse>> allocateStudentToHostel(
            @RequestBody HostelAllocationRequest request
    ) {
        HostelAllocationResponse response = hostelAllocationService.allocateStudentToHostel(request);
        ApiResponse<HostelAllocationResponse> apiResponse = new ApiResponse<>("Student allocated to hostel successfully", true, response);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<HostelAllocationResponse>>> getAllHostelAllocations() {
        List<HostelAllocationResponse> response = hostelAllocationService.getAllHostelAllocations();
        ApiResponse<List<HostelAllocationResponse>> apiResponse = new ApiResponse<>("All hostel allocations fetched successfully", true, response);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HostelAllocationResponse>> getHostelAllocationById(@PathVariable Long id) {
        Optional<HostelAllocationResponse> response = hostelAllocationService.getHostelAllocationById(id);
        return response.map(res -> ResponseEntity.ok(
                        new ApiResponse<>("Hostel allocation fetched successfully", true, res)
                ))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteHostelAllocation(@PathVariable Long id) {
        hostelAllocationService.deleteHostelAllocation(id);
        ApiResponse<Void> apiResponse = new ApiResponse<>("Hostel allocation deleted successfully", true, null);
        return ResponseEntity.ok(apiResponse);
    }

    @PatchMapping("/{id}/update-payment-status")
    public ResponseEntity<ApiResponse<HostelAllocationResponse>> updatePaymentStatus(
            @PathVariable Long id,
            @RequestParam PaymentStatus paymentStatus
    ) {
        HostelAllocationResponse updatedAllocation = hostelAllocationService.updatePaymentStatus(id, paymentStatus);
        ApiResponse<HostelAllocationResponse> apiResponse = new ApiResponse<>("Payment status updated successfully", true, updatedAllocation);
        return ResponseEntity.ok(apiResponse);
    }
}

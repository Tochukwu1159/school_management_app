package examination.teacherAndStudents.controller;

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
    public ResponseEntity<HostelAllocationResponse> payHotelAllocation(@RequestParam Long dueId, @RequestParam Long sessionId) {
        HostelAllocationResponse response = hostelAllocationService.payHotelAllocation(dueId, sessionId);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/allocate")
    public ResponseEntity<HostelAllocationResponse> allocateStudentToHostel(@RequestBody HostelAllocationRequest request) {
        HostelAllocationResponse response = hostelAllocationService.allocateStudentToHostel(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<HostelAllocationResponse>> getAllHostelAllocations() {
        List<HostelAllocationResponse> response = hostelAllocationService.getAllHostelAllocations();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HostelAllocationResponse> getHostelAllocationById(@PathVariable Long id) {
        Optional<HostelAllocationResponse> response = hostelAllocationService.getHostelAllocationById(id);
        return response.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHostelAllocation(@PathVariable Long id) {
        hostelAllocationService.deleteHostelAllocation(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/update-payment-status")
    public ResponseEntity<HostelAllocationResponse> updatePaymentStatus(
            @PathVariable Long id,
            @RequestParam PaymentStatus paymentStatus) {
        HostelAllocationResponse updatedAllocation = hostelAllocationService.updatePaymentStatus(id, paymentStatus);
        return ResponseEntity.ok(updatedAllocation);
    }
}

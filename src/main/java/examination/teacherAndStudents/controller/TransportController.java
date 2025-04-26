package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.service.TransportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transports")
public class TransportController {

    private final TransportService transportService;

    @Autowired
    public TransportController(TransportService transportService) {
        this.transportService = transportService;
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<TransportResponse>> addTransport(@RequestBody TransportRequest transportRequest) {
        TransportResponse createdTransport = transportService.createTransport(transportRequest);
        ApiResponse<TransportResponse> response = new ApiResponse<>("Transport created successfully", true, createdTransport);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/pay")
    public ResponseEntity<ApiResponse<TransportAllocationResponse>> payForTransport(@RequestBody TransportPaymentRequest transportPaymentRequest) {
        TransportAllocationResponse response = transportService.payForTransport(transportPaymentRequest);
        ApiResponse<TransportAllocationResponse> apiResponse = new ApiResponse<>("Payment successful", true, response);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/add-student-to-transport")
    public ResponseEntity<ApiResponse<TransportAllocationResponse>> addStudentToTransport(@RequestBody AddStudentToTransportRequest request) {
        TransportAllocationResponse createdTransport = transportService.assignTransportToStudent(request);
        ApiResponse<TransportAllocationResponse> response = new ApiResponse<>("Student added to transport successfully", true, createdTransport);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{transportId}/students")
    public ResponseEntity<ApiResponse<TransportResponse>> addStudentsToTransport(@PathVariable Long transportId,
                                                                                 @RequestBody List<Long> studentIds) {
        TransportResponse createdTransport = transportService.addStudentsToTransport(transportId, studentIds);
        ApiResponse<TransportResponse> response = new ApiResponse<>("Students added to transport successfully", true, createdTransport);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/edit/{transportId}")
    public ResponseEntity<ApiResponse<TransportResponse>> editTransport(@PathVariable Long transportId,
                                                                        @RequestBody TransportRequest updatedTransport) {
        TransportResponse updatedTransportation = transportService.updateTransport(transportId, updatedTransport);
        ApiResponse<TransportResponse> response = new ApiResponse<>("Transport updated successfully", true, updatedTransportation);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{transportId}")
    public ResponseEntity<ApiResponse<Void>> deleteTransport(@PathVariable Long transportId) {
        transportService.deleteTransport(transportId);
        ApiResponse<Void> response = new ApiResponse<>("Transport deleted successfully", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Page<TransportResponse>>> getAllTransports(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String vehicleNumber,
            @RequestParam(required = false) String licenceNumber,
            @RequestParam(required = false) Long driverId,
            @RequestParam(required = false) Boolean available,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        Page<TransportResponse> transportsPage = transportService.getAllTransports(
                id,
                vehicleNumber,
                licenceNumber,
                driverId,
                available,
                page,
                size,
                sortBy,
                sortDirection);

        ApiResponse<Page<TransportResponse>> response = new ApiResponse<>("Transports fetched successfully", true, transportsPage);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{transportId}")
    public ResponseEntity<ApiResponse<TransportResponse>> getTransportById(@PathVariable Long transportId) {
        TransportResponse transport = transportService.getTransportById(transportId);
        ApiResponse<TransportResponse> response = new ApiResponse<>("Transport fetched successfully", true, transport);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.entity.BusLocation;
import examination.teacherAndStudents.service.BusLocationService;
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
    private final BusLocationService busLocationService;

    @Autowired
    public TransportController(TransportService transportService, BusLocationService busLocationService) {
        this.transportService = transportService;
        this.busLocationService = busLocationService;
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<TransportResponse>> addTransport(@RequestBody TransportRequest transportRequest) {
        TransportResponse createdTransport = transportService.createTransport(transportRequest);
        ApiResponse<TransportResponse> response = new ApiResponse<>("Transport created successfully", true, createdTransport);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/add-bus")
    public ResponseEntity<ApiResponse<TransportResponse>> addBusToRoute(@RequestBody AddBusToRouteRequest request) {
        TransportResponse createdTransport = transportService.addBusToRoute(request);
        ApiResponse<TransportResponse> response = new ApiResponse<>("Bus added to route successfully", true, createdTransport);
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

    @PostMapping("/location/update")
    public ResponseEntity<ApiResponse<String>> updateBusLocation(@RequestBody BusLocationRequest request) {
        String response = busLocationService.updateBusLocation(request);
        ApiResponse<String> apiResponse = new ApiResponse<>("Bus location updated successfully", true, response);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/location/{busId}")
    public ResponseEntity<BusLocation> getBusLocation(@PathVariable Long busId) {
        BusLocation location = busLocationService.getBusLocation(busId);
        return ResponseEntity.ok(location);
    }

    @GetMapping("/allocated-students")
    public ResponseEntity<ApiResponse<Page<TransportAllocationResponse>>> getAllocatedStudentsForDriver(
            @RequestParam Long driverId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        Page<TransportAllocationResponse> allocations = transportService.getAllocatedStudentsForDriver(
                driverId, page, size, sortBy, sortDirection);
        ApiResponse<Page<TransportAllocationResponse>> apiResponse = new ApiResponse<>("Allocated students fetched successfully", true, allocations);
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{busId}/assign-driver")
    public ResponseEntity<ApiResponse<TransportResponse>> assignDriverToBus(@PathVariable Long busId,
                                                                            @RequestBody AssignDriverRequest request) {
        TransportResponse updatedTransport = transportService.assignDriverToBus(busId, request.getDriverId());
        ApiResponse<TransportResponse> response = new ApiResponse<>("Driver assigned to bus successfully", true, updatedTransport);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
package examination.teacherAndStudents.controller;
import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.entity.StudentTransportAllocation;
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

    @Autowired
    private TransportService transportService;

    @PostMapping("/add")
    public ResponseEntity<TransportResponse> addTransport(@RequestBody TransportRequest transportRequest) {
        try {
            TransportResponse createdTransport = transportService.createTransport(transportRequest);
            return new ResponseEntity<>(createdTransport, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping("/pay")
    public ResponseEntity<TransportAllocationResponse> payforTransport(@RequestBody TransportPaymentRequest transportPaymentRequest) {
        TransportAllocationResponse response = transportService.payForTransport(transportPaymentRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add-student-to-transport")
    public ResponseEntity<TransportAllocationResponse> addStudentToTransport(@RequestBody AddStudentToTransportRequest request) {
        try {
            TransportAllocationResponse createdTransport = transportService.assignTransportToStudent(request

            );
            return new ResponseEntity<>(createdTransport, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/{transportId}/students")
        public ResponseEntity<TransportResponse> addStudentsToTransport(@PathVariable Long transportId,
                @RequestBody List<Long> studentIds) {
        try {
            TransportResponse createdTransport = transportService.addStudentsToTransport(transportId, studentIds);
            return new ResponseEntity<>(createdTransport, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

            @PutMapping("/edit/{transportId}")
    public ResponseEntity<TransportResponse> editTransport(
            @PathVariable Long transportId,
            @RequestBody TransportRequest updatedTransport) {
        try {
            TransportResponse updatedTransportation = transportService.updateTransport(transportId, updatedTransport);
            return new ResponseEntity<>(updatedTransportation, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{transportId}")
    public ResponseEntity<String> deleteTransport(@PathVariable Long transportId) {
        try {
            transportService.deleteTransport(transportId);
            return new ResponseEntity<>("Transport deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<Page<TransportResponse>> getAllTransports(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String vehicleNumber,
            @RequestParam(required = false) String licenceNumber,
            @RequestParam(required = false) Long driverId,
            @RequestParam(required = false) Boolean available,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        try {
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

            return new ResponseEntity<>(transportsPage, HttpStatus.OK);
        } catch (CustomNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{transportId}")
    public ResponseEntity<TransportResponse> getTransportById(@PathVariable Long transportId) {
        try {
            TransportResponse transport = transportService.getTransportById(transportId);
            return new ResponseEntity<>(transport, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

package examination.teacherAndStudents.controller;
import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.TransactionResponse;
import examination.teacherAndStudents.dto.TransportRequest;
import examination.teacherAndStudents.dto.TransportResponse;
import examination.teacherAndStudents.entity.Transport;
import examination.teacherAndStudents.service.TransactionService;
import examination.teacherAndStudents.service.TransportService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PostMapping("/{transportId}/students/{studentId}")
    public ResponseEntity<TransportResponse> addStudentToTransport(  @PathVariable Long transportId,
                                                                     @PathVariable Long studentId) {
        try {
            TransportResponse createdTransport = transportService.addStudentToTransport(transportId, studentId);
            return new ResponseEntity<>(createdTransport, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }}


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
    public ResponseEntity<List<TransportResponse>> getAllTransports() {
        try {
            List<TransportResponse> allTransports = transportService.getAllTransports();
            return new ResponseEntity<>(allTransports, HttpStatus.OK);
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

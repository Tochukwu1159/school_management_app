package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.HostelRequest;
import examination.teacherAndStudents.dto.HostelResponse;
import examination.teacherAndStudents.entity.Hostel;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.InsufficientBalanceException;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.service.HostelService;
import examination.teacherAndStudents.utils.AvailabilityStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/hostels")
public class HostelController {

    @Autowired
    private HostelService hostelService;

    @GetMapping
    public ResponseEntity<Page<HostelResponse>> getAllHostels(
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

        return ResponseEntity.ok(hostelsPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HostelResponse> getHostelById(@PathVariable Long id) {
        HostelResponse hostel = hostelService.getHostelById(id);
        return ResponseEntity.ok(hostel);
    }

    @PostMapping("/add")
    public ResponseEntity<HostelResponse> createHostel(@RequestBody HostelRequest hostel) {
        HostelResponse createdHostel = hostelService.createHostel(hostel);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdHostel);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HostelResponse> updateHostel(@PathVariable Long id, @RequestBody HostelRequest updatedHostel) {
        HostelResponse hostel = hostelService.updateHostel(id, updatedHostel);
        return ResponseEntity.ok(hostel);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteHostel(@PathVariable Long id) {
        hostelService.deleteHostel(id);
        return ResponseEntity.ok("Hostel deleted successfully");
    }

}
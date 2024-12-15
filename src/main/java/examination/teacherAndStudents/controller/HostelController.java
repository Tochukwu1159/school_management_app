package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.HostelRequest;
import examination.teacherAndStudents.entity.Hostel;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.InsufficientBalanceException;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.service.HostelService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<List<Hostel>> getAllHostels() {
        List<Hostel> hostels = hostelService.getAllHostels();
        return ResponseEntity.ok(hostels);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Hostel> getHostelById(@PathVariable Long id) {
        Optional<Hostel> hostel = hostelService.getHostelById(id);
        return ResponseEntity.ok(hostel.get());
    }

    @PostMapping("/add")
    public ResponseEntity<Hostel> createHostel(@RequestBody HostelRequest hostel) {
        Hostel createdHostel = hostelService.createHostel(hostel);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdHostel);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Hostel> updateHostel(@PathVariable Long id, @RequestBody HostelRequest updatedHostel) {
        Hostel hostel = hostelService.updateHostel(id, updatedHostel);
        return ResponseEntity.ok(hostel);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteHostel(@PathVariable Long id) {
        hostelService.deleteHostel(id);
        return ResponseEntity.ok("Hostel deleted successfully");
    }


    @GetMapping("/available")
    public ResponseEntity<List<Hostel>> getAllAvailableHostels() {
        try {
            List<Hostel> availableHostels = hostelService.getAllAvailableHostels();
            return new ResponseEntity<>(availableHostels, HttpStatus.OK);
        } catch (CustomNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // Return unauthorized status for non-admin users
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
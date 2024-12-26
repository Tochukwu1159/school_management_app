package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.entity.ServiceOffered;
import examination.teacherAndStudents.service.ServiceOfferedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/services")
public class ServiceOfferedController {

    @Autowired
    private ServiceOfferedService serviceOfferedService;

    @PostMapping("/create")
    public ResponseEntity<ServiceOffered> createService(@RequestBody ServiceOffered serviceOffered) {
        return ResponseEntity.ok(serviceOfferedService.createServiceOffered(serviceOffered));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceOffered> updateService(@PathVariable Long id, @RequestBody ServiceOffered serviceOffered) {
        return ResponseEntity.ok(serviceOfferedService.updateServiceOffered(id, serviceOffered));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        serviceOfferedService.deleteServiceOffered(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ServiceOffered>> getAllServices() {
        return ResponseEntity.ok(serviceOfferedService.getAllServicesOffered());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceOffered> getServiceById(@PathVariable Long id) {
        return ResponseEntity.ok(serviceOfferedService.getServiceOfferedById(id));
    }
}

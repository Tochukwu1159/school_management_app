package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.StaffLevelRequest;
import examination.teacherAndStudents.dto.StaffLevelResponse;
import examination.teacherAndStudents.service.StaffLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/staff-levels")
public class StaffLevelController {

    @Autowired
    private StaffLevelService staffLevelService;

    @PostMapping
    public StaffLevelResponse createStaffLevel(@RequestBody StaffLevelRequest request) {
        return staffLevelService.createStaffLevel(request);
    }

    @PutMapping("/{id}")
    public StaffLevelResponse editStaffLevel(@PathVariable Long id, @RequestBody StaffLevelRequest request) {
        return staffLevelService.editStaffLevel(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteStaffLevel(@PathVariable Long id) {
        staffLevelService.deleteStaffLevel(id);
    }

    @GetMapping
    public List<StaffLevelResponse> getAllStaffLevels() {
        return staffLevelService.getAllStaffLevels();
    }

    @GetMapping("/{id}")
    public StaffLevelResponse getStaffLevelById(@PathVariable Long id) {
        return staffLevelService.getStaffLevelById(id);
    }
}

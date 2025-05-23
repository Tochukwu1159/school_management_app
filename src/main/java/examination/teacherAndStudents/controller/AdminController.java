package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.service.AdminService;
import examination.teacherAndStudents.utils.ProfileStatus;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class AdminController {

    private final AdminService userService;


    public AdminController(AdminService userService) {
        this.userService = userService;
    }

    @GetMapping("/students")
    public ResponseEntity<Page<User>> getAllStudents(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String middleName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) ProfileStatus profileStatus,
            @RequestParam(required = false) Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "lastName") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        try {
            Page<User> studentsPage = userService.getAllStudents(
                    firstName, lastName, middleName, email,
                    profileStatus, id, page, size, sortBy, sortDirection);

            return ResponseEntity.ok(studentsPage);
        } catch (CustomNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (CustomInternalServerException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/teachers")
    public ResponseEntity<List<User>> getAllTeachers() {
        try {
            List<User> teachersList = userService.getAllTeachers();
            return ResponseEntity.ok(teachersList);
        } catch (CustomNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // You can customize the response as needed
        } catch (CustomInternalServerException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // You can customize the response as needed
        }
    }

}
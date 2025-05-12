package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.ClassNameRequest;
import examination.teacherAndStudents.dto.ClassNameResponse;
import examination.teacherAndStudents.service.ClassNameService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/class-names")
@RequiredArgsConstructor
public class ClassNameController {

    private final ClassNameService classNameService;

    @PostMapping
    public ResponseEntity<ApiResponse<ClassNameResponse>> createClassName(@RequestBody ClassNameRequest request) {
        try {
            ClassNameResponse response = classNameService.createClassName(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("Class name created successfully", true, response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error creating class name: " + e.getMessage(), false));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClassNameResponse>> updateClassName(@PathVariable Long id, @RequestBody ClassNameRequest request) {
        try {
            ClassNameResponse response = classNameService.updateClassName(id, request);
            return ResponseEntity.ok(new ApiResponse<>("Class name updated successfully", true, response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error updating class name: " + e.getMessage(), false));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClassNameResponse>> getClassNameById(@PathVariable Long id) {
        try {
            ClassNameResponse response = classNameService.getClassNameById(id);
            return ResponseEntity.ok(new ApiResponse<>("Class name retrieved successfully", true, response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error retrieving class name: " + e.getMessage(), false));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ClassNameResponse>>> getAllClassNames(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        try {
            Page<ClassNameResponse> response = classNameService.getAllClassNames(name, page, size, sortBy, sortDirection);
            return ResponseEntity.ok(new ApiResponse<>("Class names retrieved successfully", true, response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error retrieving class names: " + e.getMessage(), false));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteClassName(@PathVariable Long id) {
        try {
            classNameService.deleteClassName(id);
            return ResponseEntity.ok(new ApiResponse<>("Class name deleted successfully", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error deleting class name: " + e.getMessage(), false));
        }
    }
}
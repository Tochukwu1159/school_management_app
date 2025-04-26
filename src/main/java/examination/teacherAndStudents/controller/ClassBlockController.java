package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.ClassBlockRequest;
import examination.teacherAndStudents.dto.ClassBlockResponse;
import examination.teacherAndStudents.dto.UpdateFormTeacherRequest;
import examination.teacherAndStudents.service.ClassBlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/class-blocks")
@RequiredArgsConstructor
public class ClassBlockController {

    private final ClassBlockService classBlockService;

    @PostMapping
    public ResponseEntity<ApiResponse<ClassBlockResponse>> createClassBlock(@RequestBody ClassBlockRequest request) {
        ClassBlockResponse response = classBlockService.createClassBlock(request);
        return ResponseEntity.status(201).body(new ApiResponse<>("Class block created successfully", true, response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ClassBlockResponse>>> getAllClassBlocks(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long subClassId,
            @RequestParam(required = false) Long academicYearId) {
        List<ClassBlockResponse> responses = classBlockService.getAllClassBlocks(classId, subClassId, academicYearId);
        return ResponseEntity.ok(new ApiResponse<>("Class blocks retrieved successfully", true, responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClassBlockResponse>> getClassBlockById(@PathVariable Long id) {
        ClassBlockResponse response = classBlockService.getClassBlockById(id);
        return ResponseEntity.ok(new ApiResponse<>("Class block retrieved successfully", true, response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClassBlockResponse>> updateClassBlock(@PathVariable Long id, @RequestBody ClassBlockRequest request) {
        ClassBlockResponse response = classBlockService.updateClassBlock(id, request);
        return ResponseEntity.ok(new ApiResponse<>("Class block updated successfully", true, response));
    }

    @PutMapping("/change-class/{studentId}")
    public ResponseEntity<ApiResponse<ClassBlockResponse>> changeStudentClass(
            @PathVariable Long studentId, @RequestBody ClassBlockRequest request) {
        ClassBlockResponse response = classBlockService.changeStudentClass(studentId, request);
        return ResponseEntity.ok(new ApiResponse<>("Student class changed successfully", true, response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteClassBlock(@PathVariable Long id) {
        classBlockService.deleteClassBlock(id);
        return ResponseEntity.ok(new ApiResponse<>("Class block deleted successfully", true));
    }

    @PutMapping("/form-teacher/update")
    public ResponseEntity<ApiResponse<ClassBlockResponse>> updateFormTeacher(
            @RequestParam Long academicYearId,
            @RequestParam Long classLevelId,
            @RequestParam Long classSubClassId,
            @RequestParam Long classTeacherId) {
        UpdateFormTeacherRequest request = UpdateFormTeacherRequest.builder()
                .subclassId(classSubClassId)
                .teacherId(classTeacherId)
                .classLevelId(classLevelId)
                .sessionId(academicYearId)
                .build();
        ClassBlockResponse response = classBlockService.updateFormTeacher(request);
        return ResponseEntity.ok(new ApiResponse<>("Form teacher updated successfully", true, response));
    }
}
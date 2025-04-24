package examination.teacherAndStudents.controller;

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
    public ResponseEntity<ClassBlockResponse> createClassBlock(@RequestBody ClassBlockRequest request) {
        ClassBlockResponse response = classBlockService.createClassBlock(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ClassBlockResponse>> getAllClassBlocks(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long subClassId,
            @RequestParam(required = false) Long academicYearId) {
        List<ClassBlockResponse> responses = classBlockService.getAllClassBlocks(
                classId,
                subClassId,
                academicYearId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassBlockResponse> getClassBlockById(@PathVariable Long id) {
        ClassBlockResponse response = classBlockService.getClassBlockById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClassBlockResponse> updateClassBlock(@PathVariable Long id, @RequestBody ClassBlockRequest request) {
        ClassBlockResponse response = classBlockService.updateClassBlock(id, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-class/{studentId}")
    public ResponseEntity<ClassBlockResponse> changeStudentClass(@PathVariable Long studentId, @RequestBody ClassBlockRequest request, @PathVariable String id) {
        ClassBlockResponse response = classBlockService.changeStudentClass(studentId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClassBlock(@PathVariable Long id) {
        classBlockService.deleteClassBlock(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/form-teacher/update")
    public ResponseEntity<ClassBlockResponse> updateFormTeacher(
            @RequestParam Long academicYearId,
            @RequestParam Long classLevelId,
            @RequestParam Long classSubClassId,
            @RequestParam Long classTeacherId
    ) {
        UpdateFormTeacherRequest  request = UpdateFormTeacherRequest.builder()
                .subclassId(classSubClassId)
                .teacherId(classTeacherId)
                .classLevelId(classLevelId)
                .sessionId(academicYearId)
                .build();

        ClassBlockResponse response = classBlockService.updateFormTeacher(request);
        return ResponseEntity.ok(response);
    }
}

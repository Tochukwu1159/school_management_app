package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.entity.ClassBlock;
import examination.teacherAndStudents.entity.ClassLevel;
import examination.teacherAndStudents.repository.ClassBlockRepository;
import examination.teacherAndStudents.service.serviceImpl.ClassLevelServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/class-levels")
public class ClassLevelController {

    private final ClassLevelServiceImpl classLevelService;
    private final ClassBlockRepository classBlockRepository;

    @Autowired
    public ClassLevelController(ClassLevelServiceImpl classLevelService, ClassBlockRepository classBlockRepository) {
        this.classLevelService = classLevelService;
        this.classBlockRepository = classBlockRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ClassLevel>>> getAllClassLevels(
            @RequestParam(required = false) Long classLevelId,
            @RequestParam(required = false) Long academicYearId,
            @RequestParam(required = false) String className,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        Page<ClassLevel> classLevels = classLevelService.getAllClassLevels(
                classLevelId, academicYearId, className, page, size, sortBy, sortDirection);
        ApiResponse<Page<ClassLevel>> response = new ApiResponse<>("Class levels retrieved successfully", true, classLevels);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClassLevel>> getClassLevelById(@PathVariable Long id) {
        ClassLevel classLevel = classLevelService.getClassLevelById(id);
        ApiResponse<ClassLevel> response = new ApiResponse<>("Class level retrieved successfully", true, classLevel);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ClassLevelWithBlocksResponse>> createClassLevel(@RequestBody @Valid ClassLevelRequest classLevelRequest) {
        ClassLevel createdClassLevel = classLevelService.createClassLevel(classLevelRequest);
        List<String> blockNames = classBlockRepository.findByClassLevelId(createdClassLevel.getId())
                .stream()
                .map(ClassBlock::getName)
                .collect(Collectors.toList());

        ClassLevelWithBlocksResponse response = ClassLevelWithBlocksResponse.builder()
                .id(createdClassLevel.getId())
                .className(ClassNameResponse.builder()
                        .id(createdClassLevel.getClassName().getId())
                        .name(createdClassLevel.getClassName().getName())
                        .build())
                .academicSessionId(createdClassLevel.getAcademicYear().getId())
                .classBlocks(blockNames)
                .build();

        ApiResponse<ClassLevelWithBlocksResponse> apiResponse = new ApiResponse<>("Class level created successfully", true, response);
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PutMapping("/url/{id}")
    public ResponseEntity<ApiResponse<ClassLevel>> updateClassLevelUrlForStudents(@PathVariable Long id, @RequestBody ClassLevelRequestUrl classLevelRequest) {
        ClassLevel updatedClassLevel = classLevelService.updateClassBlockUrl(id, classLevelRequest).getClassLevel();
        ApiResponse<ClassLevel> response = new ApiResponse<>("Class level URL updated successfully", true, updatedClassLevel);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClassLevel>> updateClassLevel(@PathVariable Long id, @RequestBody @Valid ClassLevelRequest classLevelRequest) {
        ClassLevel updatedClassLevel = classLevelService.updateClassLevel(id, classLevelRequest);
        ApiResponse<ClassLevel> response = updatedClassLevel != null
                ? new ApiResponse<>("Class level updated successfully", true, updatedClassLevel)
                : new ApiResponse<>("Class level not found", false, null);

        return updatedClassLevel != null
                ? new ResponseEntity<>(response, HttpStatus.OK)
                : new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteClassLevel(@PathVariable Long id) {
        classLevelService.deleteClassLevel(id);
        ApiResponse<Void> response = new ApiResponse<>("Class level deleted successfully", true, null);
        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }

    @GetMapping("/sub-class/{classLevelId}")
    public ResponseEntity<ApiResponse<List<ClassBlockResponse>>> getSubClassesByClassLevelId(@PathVariable Long classLevelId) {
        List<ClassBlock> subClasses = classLevelService.getSubClassesByClassLevelId(classLevelId);
        List<ClassBlockResponse> responses = subClasses.stream()
                .map(this::mapToClassBlockResponse)
                .collect(Collectors.toList());

        ApiResponse<List<ClassBlockResponse>> response = new ApiResponse<>("Class blocks retrieved successfully", true, responses);
        return ResponseEntity.ok(response);
    }

    private ClassBlockResponse mapToClassBlockResponse(ClassBlock classBlock) {
        Long formTeacherId = null;
        String formTeacherName = null;
        if (classBlock.getFormTeacher() != null && classBlock.getFormTeacher().getUser() != null) {
            formTeacherId = classBlock.getFormTeacher().getId();
            formTeacherName = classBlock.getFormTeacher().getUser().getFirstName() + " " +
                    classBlock.getFormTeacher().getUser().getLastName();
        }

        return ClassBlockResponse.builder()
                .id(classBlock.getId())
                .name(classBlock.getName())
                .classLevelId(classBlock.getClassLevel().getId())
                .classLevelName(classBlock.getClassLevel().getClassName().getName())
                .classUniqueUrl(classBlock.getClassUniqueUrl())
                .numberOfStudents(classBlock.getNumberOfStudents())
                .formTeacherId(formTeacherId)
                .formTeacherName(formTeacherName)
                .build();
    }
}
package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.entity.ClassBlock;
import examination.teacherAndStudents.entity.ClassLevel;
import examination.teacherAndStudents.repository.ClassBlockRepository;
import examination.teacherAndStudents.service.serviceImpl.ClassLevelServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    /**
     * Retrieves a paginated list of class levels with optional filters.
     */
    @GetMapping
    public ResponseEntity<Page<ClassLevel>> getAllClassLevels(
            @RequestParam(required = false) Long classLevelId,
            @RequestParam(required = false) Long academicYearId,
            @RequestParam(required = false) String className,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        Page<ClassLevel> classLevels = classLevelService.getAllClassLevels(
                classLevelId, academicYearId, className, page, size, sortBy, sortDirection);
        return new ResponseEntity<>(classLevels, HttpStatus.OK);
    }

    /**
     * Retrieves a class level by its ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClassLevel> getClassLevelById(@PathVariable Long id) {
        ClassLevel classLevel = classLevelService.getClassLevelById(id);
        return new ResponseEntity<>(classLevel, HttpStatus.OK); // Fixed status
    }

    /**
     * Creates a new class level with associated class blocks.
     */
    @PostMapping
    public ResponseEntity<ClassLevelWithBlocksResponse> createClassLevel(@RequestBody @Valid ClassLevelRequest classLevel) {
        ClassLevel createdClassLevel = classLevelService.createClassLevel(classLevel);
        List<String> blockNames = classBlockRepository.findByClassLevelId(createdClassLevel.getId())
                .stream()
                .map(ClassBlock::getName)
                .collect(Collectors.toList());

        ClassLevelWithBlocksResponse response = ClassLevelWithBlocksResponse.builder()
                .id(createdClassLevel.getId())
                .className(createdClassLevel.getClassName())
                .academicSessionId(createdClassLevel.getAcademicYear().getId())
                .classBlocks(blockNames)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Updates the unique URL for a class block.
     */
    @PutMapping("/url/{id}")
    public ResponseEntity<ClassLevel> updateClassLevelUrlForStudents(@PathVariable Long id, @RequestBody ClassLevelRequestUrl classLevel) {
        ClassLevel createdClassLevel = classLevelService.updateClassBlockUrl(id, classLevel).getClassLevel();
        return new ResponseEntity<>(createdClassLevel, HttpStatus.OK); // Fixed status
    }

    /**
     * Updates an existing class level.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ClassLevel> updateClassLevel(@PathVariable Long id, @RequestBody  @Valid ClassLevelRequest classLevelRequest) {
        ClassLevel updated = classLevelService.updateClassLevel(id, classLevelRequest);
        return updated != null
                ? new ResponseEntity<>(updated, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Deletes a class level by its ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClassLevel(@PathVariable Long id) {
        classLevelService.deleteClassLevel(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Retrieves all subclasses (class blocks) for a given class level.
     */
    @GetMapping("/sub-class/{classLevelId}")
    public ResponseEntity<List<ClassBlockResponse>> getSubClassesByClassLevelId(@PathVariable Long classLevelId) {
        List<ClassBlock> subClasses = classLevelService.getSubClassesByClassLevelId(classLevelId);
        List<ClassBlockResponse> responses = subClasses.stream()
                .map(this::mapToClassBlockResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
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
                .classLevelName(classBlock.getClassLevel().getClassName())
                .classUniqueUrl(classBlock.getClassUniqueUrl())
                .numberOfStudents(classBlock.getNumberOfStudents())
                .formTeacherId(formTeacherId)
                .formTeacherName(formTeacherName)
                .build();
    }
}
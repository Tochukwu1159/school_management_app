package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ClassLevelRequest;
import examination.teacherAndStudents.dto.ClassLevelRequestUrl;
import examination.teacherAndStudents.entity.ClassLevel;
import examination.teacherAndStudents.service.serviceImpl.ClassLevelServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/class-levels")
public class ClassLevelController {

    private final ClassLevelServiceImpl classLevelService;

    @Autowired
    public ClassLevelController(ClassLevelServiceImpl classLevelService) {
        this.classLevelService = classLevelService;
    }

    @GetMapping
    public ResponseEntity<Page<ClassLevel>> getAllClassLevels(
            @RequestParam(required = false) Long classLevelId,
            @RequestParam(required = false) Long academicYearId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        Page<ClassLevel> classLevels = classLevelService.getAllClassLevels(
                classLevelId,
                academicYearId,
                page,
                size,
                sortBy,
                sortDirection);

        return new ResponseEntity<>(classLevels, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassLevel> getClassLevelById(@PathVariable Long id) {
        Optional<ClassLevel> classLevel = classLevelService.getClassLevelById(id);
        return classLevel.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<ClassLevel> createClassLevel(@RequestBody ClassLevelRequest classLevel) {
        ClassLevel createdClassLevel = classLevelService.createClassLevel(classLevel);
        return new ResponseEntity<>(createdClassLevel, HttpStatus.CREATED);
    }

    @PutMapping ("/url/{id}")
    public ResponseEntity<ClassLevel> updateClassLevelUrlForStudents(@PathVariable Long id, @RequestBody ClassLevelRequestUrl classLevel) {
        ClassLevel createdClassLevel = classLevelService.updateClassLevelUrl(id, classLevel);
        return new ResponseEntity<>(createdClassLevel, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClassLevel> updateClassLevel(@PathVariable Long id, @RequestBody ClassLevelRequest classLevelRequest) {
        ClassLevel updated = classLevelService.updateClassLevel(id, classLevelRequest);
        return updated != null
                ? new ResponseEntity<>(updated, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClassLevel(@PathVariable Long id) {
        classLevelService.deleteClassLevel(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
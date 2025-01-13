package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.GradeRequest;
import examination.teacherAndStudents.dto.GradeResponse;
import examination.teacherAndStudents.service.GradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/grades")
public class GradeController {

    @Autowired
    private GradeService gradeService;

    @PostMapping
    public GradeResponse createGrade(@RequestBody GradeRequest gradeRequest) {
        return gradeService.createGrade(gradeRequest);
    }

    @PutMapping("/{gradeId}")
    public GradeResponse updateGrade(@PathVariable Long gradeId, @RequestBody GradeRequest gradeRequest) {
        return gradeService.updateGrade(gradeId, gradeRequest);
    }

    @DeleteMapping("/{gradeId}")
    public void deleteGrade(@PathVariable Long gradeId) {
        gradeService.deleteGradeById(gradeId);
    }

    @GetMapping("/{gradeId}")
    public GradeResponse getGradeById(@PathVariable Long gradeId) {
        return gradeService.getGradeById(gradeId);
    }

    @GetMapping("/school/{schoolId}")
    public List<GradeResponse> findAllGradesBySchool(@PathVariable Long schoolId) {
        return gradeService.findAllGradesBySchool(schoolId);
    }
}

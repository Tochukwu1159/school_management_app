package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.entity.AcademicSession;
import examination.teacherAndStudents.entity.ClassBlock;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.Result;
import examination.teacherAndStudents.error_handler.EntityNotFoundException;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.repository.AcademicSessionRepository;
import examination.teacherAndStudents.repository.ClassBlockRepository;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.service.PositionService;
import examination.teacherAndStudents.service.ResultService;
import examination.teacherAndStudents.utils.StudentTerm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/results")
public class ResultController {

    @Autowired
    private ResultService resultService;
    @Autowired
    private PositionService positionService;
    @Autowired
    private ClassBlockRepository classBlockRepository;
    @Autowired
    private AcademicSessionRepository academicSessionRepository;
    @Autowired
    private ProfileRepository profileRepository;

    @GetMapping("/calculate/{classLevelId}/{studentId}/{term}/{subjectName}")
    public ResponseEntity<Result> calculateResult(@PathVariable Long classLevelId,@PathVariable Long studentId, @PathVariable String subjectName,@PathVariable Long sessionId,  @PathVariable Long term) {
        try {
            // Assuming you have a method in the service to calculate the result
            Result result = resultService.calculateResult(classLevelId, studentId, subjectName,sessionId, term);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
           throw new RuntimeException("Error calculating result: " + e.getMessage());
        }
    }
    @PostMapping("/update-average/{sessionId}/{classLevelId}/{termId}")
    public ResponseEntity<String> calculateAverageResult(@PathVariable Long sessionId,
                                                         @PathVariable Long classLevelId,
                                                         @PathVariable Long termId) {
        try {
            resultService.calculateAverageResult(sessionId, classLevelId, termId);
            return ResponseEntity.ok("Average scores updated successfully.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Data not found: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating average scores.");
        }
    }

    @PostMapping("/promote")
    public ResponseEntity<String> promoteStudents(
            @RequestParam Long sessionId,
            @RequestParam Long presentClassId,
            @RequestParam Long futureSessionId,
            @RequestParam Long futurePClassId,
            @RequestParam Long futureFClassId,
            @RequestParam int cutOff) {
        try {
            resultService.promoteStudents(sessionId, presentClassId, futureSessionId, futurePClassId, futureFClassId, cutOff);
            return ResponseEntity.ok("Students promoted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error occurred: " + e.getMessage());
        }
    }

        @PostMapping("/update")
        public ResponseEntity<String> updateSessionAverage(@RequestParam Long classBlockId,
                                                           @RequestParam Long academicYearId) {
            // Fetch the class block and academic year
            ClassBlock classBlock = classBlockRepository.findById(classBlockId)
                    .orElseThrow(() -> new NotFoundException("Class block not found"));

            AcademicSession academicYear = academicSessionRepository.findById(academicYearId)
                    .orElseThrow(() -> new NotFoundException("Academic year not found"));

            // Fetch all student profiles in the class block
            List<Profile> studentProfiles = profileRepository.findAllByClassBlock(classBlock);

            if (studentProfiles.isEmpty()) {
                throw new NotFoundException("No students found in the specified class block");
            }

            // Update session averages
            resultService.updateSessionAverage(studentProfiles, classBlock, academicYear);

            return ResponseEntity.ok("Session averages updated successfully");
        }



    // You can add more endpoints to retrieve historical results or other related information
}

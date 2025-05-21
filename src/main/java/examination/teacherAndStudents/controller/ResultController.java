package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.SessionAverageResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.EntityNotFoundException;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.repository.AcademicSessionRepository;
import examination.teacherAndStudents.repository.ClassBlockRepository;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.repository.SessionClassRepository;
import examination.teacherAndStudents.service.PositionService;
import examination.teacherAndStudents.service.ResultService;
import examination.teacherAndStudents.utils.StudentTerm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    @Autowired
    private SessionClassRepository sessionClassRepository;

    @GetMapping("/calculate/{classLevelId}/{studentId}/{term}/{subjectName}")
    public ResponseEntity<ApiResponse<Result>> calculateResult(@PathVariable Long classLevelId,@PathVariable Long studentId, @PathVariable String subjectName,@PathVariable Long sessionId,  @PathVariable Long term) {
        try {
            // Assuming you have a method in the service to calculate the result
            Result result = resultService.calculateResult(classLevelId, studentId, subjectName,sessionId, term);
            return ResponseEntity.ok(new ApiResponse<>("Result calculated successfully", true, result));
        } catch (Exception e) {
           throw new RuntimeException("Error calculating result: " + e.getMessage());
        }
    }

    @PostMapping("/update-average/{sessionId}/{classLevelId}/{termId}")
    public ResponseEntity<ApiResponse<String>> calculateAverageResult(
            @PathVariable Long sessionId,
            @PathVariable Long classLevelId,
            @PathVariable Long termId) {
        try {
            resultService.calculateAverageResult(sessionId, classLevelId, termId);
            ApiResponse<String> response = new ApiResponse<>("Average scores updated successfully.", true);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            ApiResponse<String> response = new ApiResponse<>("Data not found: " + e.getMessage(), false);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error updating average scores.", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/promote")
    public ResponseEntity<ApiResponse<String>> promoteStudents(
            @RequestParam Long sessionId,
            @RequestParam Long presentClassId,
            @RequestParam Long futureSessionId,
            @RequestParam Long futurePClassId,
            @RequestParam Long futureFClassId,
            @RequestParam int cutOff) {
        try {
            resultService.promoteStudents(sessionId, presentClassId, futureSessionId, futurePClassId, futureFClassId, cutOff);
            return ResponseEntity.ok(new ApiResponse<>("Students promoted successfully", true, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("Error occurred: " + e.getMessage(), false, null));
        }
    }


    @PostMapping("/session-average/update")
    public ResponseEntity<ApiResponse<String>> updateSessionAverage(@RequestParam Long classBlockId,
                                                                    @RequestParam Long academicYearId) {
        SessionClass sessionClass = sessionClassRepository.findBySessionIdAndClassBlockId(academicYearId, classBlockId )
                .orElseThrow(() -> new NotFoundException("Session class not found"));


        // Fetch all student profiles in the class block
        Set<Profile> studentProfiles = sessionClass.getProfiles();

        if (studentProfiles.isEmpty()) {
            throw new NotFoundException("No students found in the specified class block");
        }

        // Update session averages
        resultService.updateSessionAverage(studentProfiles, sessionClass);

        return ResponseEntity.ok(new ApiResponse<>("Session averages updated successfully", true, null));
    }


    @GetMapping("/top5/{classBlockId}/{academicSessionId}")
    public ResponseEntity<ApiResponse<List<SessionAverageResponse>>> getTop5StudentsPerClass(
            @PathVariable Long classBlockId, @PathVariable Long academicSessionId) {


        List<SessionAverage> top5Students = resultService.getTop5StudentsPerClass(classBlockId, academicSessionId);

        List<SessionAverageResponse> response = top5Students.stream()
                .map(SessionAverageResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>("Top 5 students retrieved successfully", true, response));
    }



    @GetMapping("/top5/{academicSessionId}")
    public ResponseEntity<ApiResponse<Map<String, List<SessionAverageResponse>>>> getTop5StudentsForAllClasses(
            @PathVariable Long academicSessionId) {

        Map<ClassBlock, List<SessionAverage>> top5Students =
                resultService.getTop5StudentsForAllClasses(academicSessionId);

        Map<String, List<SessionAverageResponse>> response = top5Students.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().getName(),
                        entry -> entry.getValue().stream()
                                .map(SessionAverageResponse::fromEntity)
                                .collect(Collectors.toList())
                ));

        return ResponseEntity.ok(new ApiResponse<>("Top 5 students for all classes retrieved successfully", true, response));
    }




    // You can add more endpoints to retrieve historical results or other related information
}

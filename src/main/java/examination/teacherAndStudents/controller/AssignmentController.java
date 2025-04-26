package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.AssignmentFilter;
import examination.teacherAndStudents.dto.AssignmentRequest;
import examination.teacherAndStudents.dto.AssignmentResponse;
import examination.teacherAndStudents.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@RestController
@RequestMapping("/api/v1/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<AssignmentResponse>> createAssignment(@Valid @RequestBody AssignmentRequest request) {
        AssignmentResponse response = assignmentService.saveAssignment(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        ApiResponse<AssignmentResponse> apiResponse = new ApiResponse<>("Assignment created successfully", true, response);
        return ResponseEntity.created(location).body(apiResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AssignmentResponse>> updateAssignment(@PathVariable Long id, @Valid @RequestBody AssignmentRequest request) {
        AssignmentResponse response = assignmentService.updateAssignment(id, request);
        ApiResponse<AssignmentResponse> apiResponse = new ApiResponse<>("Assignment updated successfully", true, response);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AssignmentResponse>> getAssignment(@PathVariable Long id) {
        AssignmentResponse response = assignmentService.getAssignmentById(id);
        ApiResponse<AssignmentResponse> apiResponse = new ApiResponse<>("Assignment retrieved successfully", true, response);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AssignmentResponse>>> getAllAssignments(
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateIssuedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateIssuedTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDueFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDueTo,
            @RequestParam(required = false) Long classBlockId,
            @PageableDefault(size = 20, sort = "dateDue", direction = Sort.Direction.ASC) Pageable pageable) {

        AssignmentFilter filter = new AssignmentFilter();
        filter.setTeacherId(teacherId);
        filter.setSubjectId(subjectId);
        filter.setTitle(title);
        filter.setDateIssuedFrom(dateIssuedFrom);
        filter.setDateIssuedTo(dateIssuedTo);
        filter.setDateDueFrom(dateDueFrom);
        filter.setDateDueTo(dateDueTo);
        filter.setClassBlockId(classBlockId);

        Page<AssignmentResponse> responses = assignmentService.getAllAssignments(filter, pageable);
        ApiResponse<Page<AssignmentResponse>> apiResponse = new ApiResponse<>("Assignments retrieved successfully", true, responses);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAssignment(@PathVariable Long id) {
        assignmentService.deleteAssignment(id);
        ApiResponse<Void> apiResponse = new ApiResponse<>("Assignment deleted successfully", true);
        return ResponseEntity.ok(apiResponse);
    }
}
package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.VisitorsRequest;
import examination.teacherAndStudents.dto.VisitorsResponse;
import examination.teacherAndStudents.service.VisitorsService;
import examination.teacherAndStudents.utils.VisitorStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/visitors")
public class VisitorsController {

    private final VisitorsService visitorsService;

    @Autowired
    public VisitorsController(VisitorsService visitorsService) {
        this.visitorsService = visitorsService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VisitorsResponse>> addVisitor(@RequestBody VisitorsRequest request) {
        VisitorsResponse response = visitorsService.addVisitor(request);
        ApiResponse<VisitorsResponse> apiResponse = new ApiResponse<>("Visitor added successfully", true, response);
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VisitorsResponse>> editVisitor(@PathVariable Long id, @RequestBody VisitorsRequest request) {
        VisitorsResponse response = visitorsService.editVisitor(id, request);
        ApiResponse<VisitorsResponse> apiResponse = new ApiResponse<>("Visitor updated successfully", true, response);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVisitor(@PathVariable Long id) {
        visitorsService.deleteVisitor(id);
        ApiResponse<Void> apiResponse = new ApiResponse<>("Visitor deleted successfully", true);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    public ResponseEntity<Page<ApiResponse<VisitorsResponse>>> getAllVisitors(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) VisitorStatus status,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        Page<VisitorsResponse> visitorsPage = visitorsService.getAllVisitors(
                name, phoneNumber, email, status, pageNo, pageSize, sortBy, sortDirection);

        Page<ApiResponse<VisitorsResponse>> apiResponsePage = new PageImpl<>(
                visitorsPage.getContent().stream()
                        .map(visitor -> new ApiResponse<>("Visitor retrieved successfully", true, visitor))
                        .collect(Collectors.toList()),
                visitorsPage.getPageable(),
                visitorsPage.getTotalElements()
        );

        return ResponseEntity.ok(apiResponsePage);
    }
}

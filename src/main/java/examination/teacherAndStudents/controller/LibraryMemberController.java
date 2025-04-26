package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.LibraryMemberResponse;
import examination.teacherAndStudents.dto.LibraryMembershipRequest;
import examination.teacherAndStudents.service.LibraryMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/library-members")
@RequiredArgsConstructor
public class LibraryMemberController {

    private static final Logger logger = LoggerFactory.getLogger(LibraryMemberController.class);

    private final LibraryMemberService libraryMemberService;

    @PostMapping
    public ResponseEntity<ApiResponse<LibraryMemberResponse>> createLibraryMember(
            @Valid @RequestBody LibraryMembershipRequest request) {
        logger.info("Request to create library membership for user: {}", request.getUserUniqueRegistrationNumber());
        LibraryMemberResponse response = libraryMemberService.createLibraryMember(request);
        ApiResponse<LibraryMemberResponse> apiResponse = new ApiResponse<>("Library member created successfully", true, response);
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LibraryMemberResponse>> updateLibraryMember(
            @PathVariable Long id, @Valid @RequestBody LibraryMembershipRequest request) {
        logger.info("Request to update library membership ID: {}", id);
        LibraryMemberResponse response = libraryMemberService.updateLibraryMember(id, request);
        ApiResponse<LibraryMemberResponse> apiResponse = new ApiResponse<>("Library member updated successfully", true, response);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LibraryMemberResponse>> getLibraryMemberById(@PathVariable Long id) {
        logger.info("Request to fetch library membership ID: {}", id);
        LibraryMemberResponse response = libraryMemberService.findById(id);
        ApiResponse<LibraryMemberResponse> apiResponse = new ApiResponse<>("Library member fetched successfully", true, response);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Page<ApiResponse<LibraryMemberResponse>>> getAllLibraryMembers(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        logger.info("Request to fetch all library members, page: {}, size: {}", pageNo, pageSize);
        Page<LibraryMemberResponse> responsePage = libraryMemberService.findAll(pageNo, pageSize, sortBy, sortDirection);

        Page<ApiResponse<LibraryMemberResponse>> apiResponsePage = new PageImpl<>(
                responsePage.getContent().stream()
                        .map(member -> new ApiResponse<>("Library member fetched successfully", true, member))
                        .collect(Collectors.toList()),
                responsePage.getPageable(),
                responsePage.getTotalElements()
        );

        return new ResponseEntity<>(apiResponsePage, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteLibraryMember(@PathVariable Long id) {
        logger.info("Request to delete library membership ID: {}", id);
        libraryMemberService.deleteLibraryMember(id);
        ApiResponse<String> apiResponse = new ApiResponse<>("Library member deleted successfully", true, null);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}

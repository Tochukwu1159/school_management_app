package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.LibraryMemberResponse;
import examination.teacherAndStudents.dto.LibraryMembershipRequest;
import examination.teacherAndStudents.service.LibraryMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/library-members")
@RequiredArgsConstructor
public class LibraryMemberController {

    private static final Logger logger = LoggerFactory.getLogger(LibraryMemberController.class);

    private final LibraryMemberService libraryMemberService;

    @PostMapping
    public ResponseEntity<LibraryMemberResponse> createLibraryMember(
            @Valid @RequestBody LibraryMembershipRequest request) {
        logger.info("Request to create library membership for user: {}", request.getUserUniqueRegistrationNumber());
        LibraryMemberResponse response = libraryMemberService.createLibraryMember(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LibraryMemberResponse> updateLibraryMember(
            @PathVariable Long id, @Valid @RequestBody LibraryMembershipRequest request) {
        logger.info("Request to update library membership ID: {}", id);
        LibraryMemberResponse response = libraryMemberService.updateLibraryMember(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LibraryMemberResponse> getLibraryMemberById(@PathVariable Long id) {
        logger.info("Request to fetch library membership ID: {}", id);
        LibraryMemberResponse response = libraryMemberService.findById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Page<LibraryMemberResponse>> getAllLibraryMembers(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        logger.info("Request to fetch all library members, page: {}, size: {}", pageNo, pageSize);
        Page<LibraryMemberResponse> response = libraryMemberService.findAll(pageNo, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLibraryMember(@PathVariable Long id) {
        logger.info("Request to delete library membership ID: {}", id);
        libraryMemberService.deleteLibraryMember(id);
        return new ResponseEntity<>(null, HttpStatus.OK);
    }
}
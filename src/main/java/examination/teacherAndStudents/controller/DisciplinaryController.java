package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.DisciplinaryActionRequest;
import examination.teacherAndStudents.dto.DisciplinaryActionResponse;
import examination.teacherAndStudents.entity.DisciplinaryAction;
import examination.teacherAndStudents.service.DisciplinaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/disciplinary")
@RequiredArgsConstructor
public class DisciplinaryController {

    private final DisciplinaryService disciplinaryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<DisciplinaryActionResponse>> issueDisciplinaryAction(@Valid @RequestBody DisciplinaryActionRequest request) {
        DisciplinaryActionResponse response = disciplinaryService.issueDisciplinaryAction(request);
        ApiResponse<DisciplinaryActionResponse> apiResponse = new ApiResponse<>("Disciplinary action issued successfully",true,response);
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PutMapping("/{actionId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<Void>> updateDisciplinaryAction(@PathVariable Long actionId, @Valid @RequestBody DisciplinaryActionRequest request) {
        disciplinaryService.updateDisciplinaryAction(actionId, request);
        ApiResponse<Void> apiResponse = new ApiResponse<>("Disciplinary action updated successfully",true,null);

        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    @DeleteMapping("/{actionId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<Void>> cancelDisciplinaryAction(@PathVariable Long actionId) {
        disciplinaryService.cancelDisciplinaryAction(actionId);
        ApiResponse<Void> apiResponse = new ApiResponse<>("Disciplinary action cancelled successfully",true,null);

        return new ResponseEntity<>(apiResponse,HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{actionId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<DisciplinaryActionResponse>> getDisciplinaryActionById(@PathVariable Long actionId) {
        DisciplinaryActionResponse response = disciplinaryService.getDisciplinaryActionById(actionId);
        ApiResponse<DisciplinaryActionResponse> apiResponse = new ApiResponse<>("Disciplinary action fetched successfully",true,response);

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<Page<DisciplinaryActionResponse>>> getAllActiveDisciplinaryActions(Pageable pageable) {
        Page<DisciplinaryActionResponse> actions = disciplinaryService.getAllActiveDisciplinaryActions(pageable);
        ApiResponse<Page<DisciplinaryActionResponse>> apiResponse = new ApiResponse<>(
                "Disciplinary actions fetched successfully",
                true,
                actions
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }


    @GetMapping("/profile/{profileId}/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<List<DisciplinaryAction>>> getActiveActionsForProfile(@PathVariable Long profileId) {

        List<DisciplinaryAction> actions = disciplinaryService.getActiveActionsForProfile(profileId);
        ApiResponse<List<DisciplinaryAction>> apiResponse = new ApiResponse<>(
                "Active disciplinary actions for profile fetched successfully",
                true,
                actions
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/profile/{profileId}/suspended")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<Boolean>> isProfileSuspended(@PathVariable Long profileId) {
        boolean isSuspended = disciplinaryService.isProfileSuspended(profileId);
        ApiResponse<Boolean> apiResponse = new ApiResponse<>(
                "Profile suspension status fetched successfully",
                true,
                isSuspended
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/deactivate-expired")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateExpiredActions() {
        disciplinaryService.deactivateExpiredActions();
        ApiResponse<Void> apiResponse = new ApiResponse<>(
                "Expired disciplinary actions deactivated successfully",
                true,
                null
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}
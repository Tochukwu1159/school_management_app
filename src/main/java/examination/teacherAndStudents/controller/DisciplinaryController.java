package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.DisciplinaryActionDTO;
import examination.teacherAndStudents.entity.DisciplinaryAction;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.service.DisciplinaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/disciplinary-actions")
@RequiredArgsConstructor
public class DisciplinaryController {

    private final DisciplinaryService disciplinaryService;
    private final ProfileRepository profileRepository
            ;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'HEAD_TEACHER')")
    public ResponseEntity<DisciplinaryAction> issueDisciplinaryAction(
            @Valid @RequestBody DisciplinaryActionDTO actionDTO) {

        String email = SecurityConfig.getAuthenticatedUserEmail();
        Profile issuedBy = profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));

        Profile profile = getProfileById(actionDTO.getRegNo());


        DisciplinaryAction action = disciplinaryService.issueDisciplinaryAction(
                profile,
                issuedBy,
                actionDTO.getActionType(),
                actionDTO.getReason(),
                actionDTO.getDescription(),
                actionDTO.getStartDate(),
                actionDTO.getEndDate());

        return ResponseEntity.created(URI.create("/api/disciplinary-actions/" + action.getId()))
                .body(action);
    }

    @GetMapping("/profile/{profileId}")
    public ResponseEntity<List<DisciplinaryAction>> getProfileDisciplinaryActions(
            @PathVariable String profileId) {
        Profile profile = getProfileById(profileId);
        List<DisciplinaryAction> actions = disciplinaryService.getActiveActionsForProfile(profile);
        return ResponseEntity.ok(actions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DisciplinaryAction> getDisciplinaryActionById(@PathVariable Long id) {
        return ResponseEntity.ok(disciplinaryService.getDisciplinaryActionById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'HEAD_TEACHER')")
    public ResponseEntity<DisciplinaryAction> updateDisciplinaryAction(
            @PathVariable Long id,
            @Valid @RequestBody DisciplinaryActionDTO actionDTO) {

        disciplinaryService.updateDisciplinaryAction(
                id,
                actionDTO.getReason(),
                actionDTO.getDescription(),
                actionDTO.getEndDate(),
                actionDTO.isActive());

        return ResponseEntity.ok(disciplinaryService.getDisciplinaryActionById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    public ResponseEntity<Void> cancelDisciplinaryAction(@PathVariable Long id) {
        disciplinaryService.cancelDisciplinaryAction(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/check-suspension/{profileId}")
    public ResponseEntity<Boolean> isProfileSuspended(@PathVariable String profileId) {
        Profile profile = getProfileById(profileId);
        return ResponseEntity.ok(disciplinaryService.isProfileSuspended(profile));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'HEAD_TEACHER')")
    public ResponseEntity<List<DisciplinaryAction>> getAllActiveDisciplinaryActions() {
        return null;
//                ResponseEntity.ok(disciplinaryService.getAllActiveDisciplinaryActions());
    }

Profile  getProfileById(String regNo) {
    return profileRepository.findByUniqueRegistrationNumber(regNo) .orElseThrow(() -> new CustomNotFoundException("Profile not found"));
}}
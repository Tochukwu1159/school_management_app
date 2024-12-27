package examination.teacherAndStudents.controller;
import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.BadRequestException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.error_handler.ResourceNotFoundException;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.service.UserService;
import examination.teacherAndStudents.utils.AccountUtils;
import examination.teacherAndStudents.utils.ProfileStatus;
import examination.teacherAndStudents.utils.Roles;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping(value="/api/v1/users")
public class UserController {

    private final UserService userService;
    private final ProfileRepository profileRepository;

    @PostMapping("/create")
    public UserResponse createStudent(@RequestBody @Valid UserRequestDto userRequest) throws MessagingException {
        return userService.createStudent(userRequest);
    }

    @PostMapping("/admin/create")
    public UserResponse createAdmin(@RequestBody @Valid UserRequestDto userRequest) throws MessagingException {
        return userService.createAdmin(userRequest);
    }

    @PostMapping("/teacher/create")
    public UserResponse createTeacher(@RequestBody @Valid UserRequestDto userRequest) throws MessagingException {
        return userService.createTeacher(userRequest);
    }

    @GetMapping("/findAll")
    public ResponseEntity<Page<UserResponse>> findAllStudentsFilteredAndPaginated(
            @RequestParam Long classCategoryId,
            @RequestParam Long subClassId,
            @RequestParam Long academicYearId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy) {
        Page<UserResponse> allStudents = userService.getAllStudentsFilteredAndPaginated(classCategoryId, subClassId, academicYearId, pageNo, pageSize, sortBy);
        return new ResponseEntity<>(allStudents, HttpStatus.OK);
    }


    @PostMapping("/login")
    public LoginResponse loginUser(@RequestBody @Valid LoginRequest loginRequest) {
        return userService.loginUser(loginRequest);

    }


    @PostMapping("/admin/login")
    public LoginResponse loginAmin(@RequestBody @Valid LoginRequest loginRequest) {
        return userService.loginAdmin(loginRequest);

    }

    @PostMapping("/edit")
    public UserResponse editUserDetails(@RequestBody @Valid EditUserRequest editUserDto) {
        return userService.editUserDetails(editUserDto);
    }

    @PostMapping("/forgot-password")
    public UserResponse forgotPassword(@RequestBody @Valid ForgotPasswordRequest forgotPasswordRequest) {
        return userService.forgotPassword(forgotPasswordRequest);
    }

    @GetMapping("/resetPassword")
    public UserResponse resetPassword(@RequestBody @Valid PasswordResetRequest passwordResetRequest, @RequestParam("token") String token) {
        return userService.resetPassword(passwordResetRequest, token);
    }



    @PostMapping("/update")
    public UserResponse updatePassword(@RequestBody @Valid ChangePasswordRequest changePasswordRequest) {
        return userService.updatePassword(changePasswordRequest);
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        boolean deleted = userService.deleteUser(userId).hasBody();
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/delete/{uniqueRegistrationNumber}")
    public ResponseEntity<UserResponse> geenerateIdCard(@PathVariable String uniqueRegistrationNumber) {
        UserResponse userResponse = userService.geenerateIdCard(uniqueRegistrationNumber);
        return new ResponseEntity<>(userResponse, HttpStatus.OK);

    }

    @PutMapping("/{studentId}/updateClassLevel")
    public ResponseEntity<UserResponse> updateStudentClassLevel(
            @PathVariable Long studentId,
            @RequestParam Long newSubClassLevelId) {
        try {
//            UserResponse response = userService.updateStudentClassLevel(studentId, newSubClassLevelId);
//            return ResponseEntity.ok(response);
        } catch (CustomNotFoundException e) {
            throw new ResourceNotFoundException("User not found " + e);
        } catch (BadRequestException e) {
            throw new ResourceNotFoundException("Update failed " + e);
        }
        return null;
    }


    @PatchMapping("/{userId}/status")
    public ResponseEntity<String> updateUserStatus(@PathVariable Long userId,
                                                   @RequestBody UserStatusUpdateRequest statusUpdateRequest) {
        try {
            // Convert action to ProfileStatus and update user status
            ProfileStatus newStatus = ProfileStatus.valueOf(statusUpdateRequest.getAction().toUpperCase());
            String responseMessage = userService.updateUserStatus(userId, newStatus, statusUpdateRequest.getSuspensionEndDate()); // No need for suspensionEndDate here
            return ResponseEntity.ok(responseMessage);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid status action.");
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating user status: " + e.getMessage());
        }
    }
//
//
@GetMapping("/profiles")
public ResponseEntity<Page<UserProfileResponse>> getProfiles(
        @RequestParam("role") String role,
        @RequestParam("status") String status,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "10") int size) {

    role = role.toUpperCase();
    status = status.toUpperCase();
    Page<UserProfileResponse> profiles = userService.getProfilesByRoleAndStatus(role, status, page, size);

    return ResponseEntity.ok(profiles);
}

}

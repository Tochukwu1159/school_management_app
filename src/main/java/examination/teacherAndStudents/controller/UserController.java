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
    public ResponseEntity<ApiResponse<UserResponse>> createStudent(@RequestBody @Valid UserRequestDto userRequest) throws MessagingException {
        UserResponse userResponse = userService.createStudent(userRequest); // Your actual logic here
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>("Student created successfully", true, userResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/self-onboarding")
    public ResponseEntity<ApiResponse<UserResponse>> selfRegisterStudent(@RequestBody @Valid UserRequestDto userRequest) throws MessagingException {
        UserResponse userResponse = userService.selfRegisterStudent(userRequest); // Your actual logic here
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>("Self-registration successful", true, userResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/admin/create")
    public ResponseEntity<ApiResponse<UserResponse>> createAdmin(@RequestBody @Valid UserRequestDto userRequest) throws MessagingException {
        UserResponse userResponse = userService.createAdmin(userRequest); // Your actual logic here
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>("Admin created successfully", true, userResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/teacher/create")
    public ResponseEntity<ApiResponse<UserResponse>> createStaff(@RequestBody @Valid UserRequestDto userRequest) throws MessagingException {
        UserResponse userResponse = userService.createStaff(userRequest); // Your actual logic here
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>("Staff created successfully", true, userResponse);
        return ResponseEntity.ok(apiResponse);
    }


    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>>  loginUser(@RequestBody @Valid LoginRequest loginRequest) {
        LoginResponse loginResponse =userService.loginUser(loginRequest); // Your actual logic here
        ApiResponse<LoginResponse> apiResponse = new ApiResponse<>("Login successful", true, loginResponse);
        return ResponseEntity.ok(apiResponse);

    }


    @PostMapping("/admin/login")
    public ResponseEntity<ApiResponse<LoginResponse>> loginAmin(@RequestBody @Valid LoginRequest loginRequest) {
        LoginResponse loginResponse = userService.loginAdmin(loginRequest); // Your actual logic here
        ApiResponse<LoginResponse> apiResponse = new ApiResponse<>("Admin login successful", true, loginResponse);
        return ResponseEntity.ok(apiResponse);

    }

    @PostMapping("/edit")
    public ResponseEntity<ApiResponse<UserResponse>> editUserDetails(@RequestBody @Valid EditUserRequest editUserDto) {
        UserResponse userResponse = userService.editUserDetails(editUserDto); // Your actual logic here
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>("User details updated successfully", true, userResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<UserResponse>> forgotPassword(@RequestBody @Valid ForgotPasswordRequest forgotPasswordRequest) {
        UserResponse userResponse = userService.forgotPassword(forgotPasswordRequest); // Your actual logic here
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>("Password reset link sent", true, userResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/resetPassword")
    public ResponseEntity<ApiResponse<UserResponse>> resetPassword(@RequestBody @Valid PasswordResetRequest passwordResetRequest, @RequestParam("token") String token) {
        UserResponse userResponse = userService.resetPassword(passwordResetRequest,token); // Your actual logic here
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>("Password reset successfully", true, userResponse);
        return ResponseEntity.ok(apiResponse);
    }



    @PostMapping("/update")
    public ResponseEntity<ApiResponse<UserResponse>> updatePassword(@RequestBody @Valid ChangePasswordRequest changePasswordRequest) {
        UserResponse userResponse = new UserResponse(); // Your actual logic here
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>("Password updated successfully", true, userResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/findAll")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> findAllStudentsFilteredAndPaginated(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long subClassId,
            @RequestParam(required = false) Long academicYearId,
            @RequestParam(required = false) String uniqueRegistrationNumber,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy) {

        Page<UserResponse> allStudents = userService.getAllStudentsFilteredAndPaginated(
                classId, subClassId, academicYearId,
                uniqueRegistrationNumber, firstName, lastName,
                pageNo, pageSize, sortBy
        );

        ApiResponse<Page<UserResponse>> response = new ApiResponse<>("Students retrieved successfully", true, allStudents);
        return ResponseEntity.ok(response);
    }



    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long userId) {
        boolean deleted = userService.deleteUser(userId).hasBody();
        if (deleted) {
            return ResponseEntity.ok(new ApiResponse<>("User deleted successfully", true));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>("User not found", false));
        }
    }

    @GetMapping("/card/{uniqueRegistrationNumber}")
    public ResponseEntity<ApiResponse<UserResponse>> generateIdCard(@PathVariable String uniqueRegistrationNumber) {
        UserResponse userResponse = userService.generateIdCard(uniqueRegistrationNumber);
        ApiResponse<UserResponse> response = new ApiResponse<>("ID Card generated successfully", true, userResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active-users")
    public ResponseEntity<ApiResponse<SchoolActiveUsersResponse>> getActiveUsersStatistics() {
        ApiResponse<SchoolActiveUsersResponse> response = new ApiResponse<>("Active users statistics fetched successfully", true, userService.getActiveUsersStatistics());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{studentId}/updateClassLevel")
    public ResponseEntity<ApiResponse<UserResponse>> updateStudentClassLevel(
            @PathVariable Long studentId,
            @RequestParam Long newSubClassLevelId) {
        try {
//            UserResponse updatedStudent = userService.updateStudentClassLevel(studentId, newSubClassLevelId);
//            ApiResponse<UserResponse> response = new ApiResponse<>("Class level updated successfully", true, updatedStudent);
            ApiResponse<UserResponse> response  = null;
            return ResponseEntity.ok(response);
        } catch (CustomNotFoundException e) {
            throw new ResourceNotFoundException("User not found: " + e.getMessage());
        } catch (BadRequestException e) {
            throw new ResourceNotFoundException("Update failed: " + e.getMessage());
        }
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<String>> updateUserStatus(@PathVariable Long userId,
                                                                @RequestBody UserStatusUpdateRequest statusUpdateRequest) {
        try {
            ProfileStatus newStatus = ProfileStatus.valueOf(statusUpdateRequest.getAction().toUpperCase());
            String responseMessage = userService.updateUserStatus(userId, newStatus, statusUpdateRequest.getSuspensionEndDate());
            return ResponseEntity.ok(new ApiResponse<>("Status updated successfully", true, responseMessage));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("Invalid status action", false));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(e.getMessage(), false));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>("Error updating user status: " + e.getMessage(), false));
        }
    }
//
@GetMapping("/profiles")
public ResponseEntity<ApiResponse<Page<UserProfileResponse>>> getProfiles(
        @RequestParam("role") String role,
        @RequestParam("status") String status,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "10") int size) {

    role = role.toUpperCase();
    status = status.toUpperCase();
    Page<UserProfileResponse> profiles = userService.getProfilesByRoleAndStatus(role, status, page, size);

    ApiResponse<Page<UserProfileResponse>> response = new ApiResponse<>("Profiles retrieved successfully", true, profiles);
    return ResponseEntity.ok(response);
}
}


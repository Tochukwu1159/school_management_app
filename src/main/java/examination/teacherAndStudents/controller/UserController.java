package examination.teacherAndStudents.controller;
import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.BadRequestException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.ResourceNotFoundException;
import examination.teacherAndStudents.service.UserService;
import examination.teacherAndStudents.utils.AccountUtils;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping(value="/api/v1/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/create")
    public UserResponse createStudent(@RequestBody @Valid UserRequestDto userRequest) throws MessagingException {
        return userService.createStudent(userRequest);
    }

    @PostMapping("/admin/create")
    public UserResponse createAdmin(@RequestBody @Valid UserRequestDto userRequest) throws MessagingException {
        return userService.createAdmin(userRequest);
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

    @PostMapping("/deactivate/{uniqueRegistrationNumber}")
    public User deactivateStudent(@PathVariable String uniqueRegistrationNumber) {
        return userService.deactivateStudent(uniqueRegistrationNumber);

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
//
//
}

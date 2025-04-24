package examination.teacherAndStudents.service;
import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.utils.ProfileStatus;
import jakarta.mail.MessagingException;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

public interface UserService {
    UserResponse  createStudent(UserRequestDto userRequest) throws MessagingException;
    //    UserResponse editStudent(EditUserRequest editUserDto);
    UserResponse createAdmin(UserRequestDto userRequest) throws MessagingException;
    LoginResponse loginAdmin(LoginRequest loginRequest);
    UserResponse createStaff(UserRequestDto userRequest) throws MessagingException;

    LoginResponse loginUser(LoginRequest loginRequest);
    UserResponse selfRegisterStudent(UserRequestDto userRequest);

    UserResponse editUserDetails(EditUserRequest editUserDto);

    UserResponse forgotPassword(ForgotPasswordRequest forgotPasswordRequest);

    UserResponse resetPassword(PasswordResetRequest passwordRequest, String token);
//    public UserResponse forgotPassword1(ForgotPasswordRequest forgotPasswordRequest);

    UserResponse updatePassword(ChangePasswordRequest changePasswordRequest);

    //    AllUserResponse getAllUsers();
    UserResponse getUser();
    ResponseEntity<Void> deleteUser(Long userId);
    UserResponse generateIdCard(String uniqueRegistrationNumber);
    String updateUserStatus(Long userId, ProfileStatus newStatus, LocalDate suspensionEndDate);
    SchoolActiveUsersResponse getActiveUsersStatistics();
    Page<UserResponse> getAllStudentsFilteredAndPaginated(
            Long classId,
            Long subClassId,
            Long academicYearId,
            String uniqueRegistrationNumber,
            String firstName,
            String lastName,
            int page,
            int size,
            String sortBy);
    Page<UserProfileResponse> getProfilesByRoleAndStatus(String role, String status, int page, int size);


}

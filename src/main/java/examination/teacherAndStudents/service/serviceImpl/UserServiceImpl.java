package examination.teacherAndStudents.service.serviceImpl;
import com.cloudinary.Cloudinary;
import com.fasterxml.jackson.core.JsonProcessingException;
import examination.teacherAndStudents.Security.CustomUserDetailService;
import examination.teacherAndStudents.Security.JwtUtil;
import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.*;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.EmailService;
import examination.teacherAndStudents.service.UserService;
import examination.teacherAndStudents.templateService.IdCardService;
import examination.teacherAndStudents.utils.*;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final  Cloudinary cloudinary;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailService customUserDetailsService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final ClassLevelRepository classLevelRepository;
    private final ClassBlockRepository classBlockRepository;
    private final WalletRepository walletRepository;
    private final ProfileRepository profileRepository;
    private final ModelMapper modelMapper;

    private final IdCardService idCardService;

    private  final PasswordGenerator passwordGenerator;
    private final SubjectRepository subjectRepository;
    private final SchoolRepository schoolRepository;
    private final StaffLevelRepository staffLevelRepository;
    private final AcademicSessionRepository academicSessionRepository;


    @Override
    @Transactional
    public UserResponse createStudent(UserRequestDto userRequest) throws MessagingException {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN)
                .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));


        School school = admin.getSchool();

        validateUserRequest(userRequest);

        ClassBlock classBlock = classBlockRepository.findById(userRequest.getClassAssignedId())
                .orElseThrow(() -> new BadRequestException("Error: Class block not found"));

        ClassLevel classLevel = classLevelRepository.findByClassName(classBlock.getClassLevel().getClassName());
        if (classLevel == null) {
            throw new BadRequestException("Error: Class level not found");
        }
//        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
//        // Get the secure URL of the uploaded image from Cloudinary
//        String imageUrl = (String) uploadResult.get("secure_url");
        String generatedPassword = passwordGenerator.generateRandomPassword();
        String encodedPassword = passwordEncoder.encode(generatedPassword);

        User newUser = User.builder()
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .middleName(userRequest.getMiddleName())
                .school(school)
                .email(userRequest.getEmail())
                .roles(Roles.STUDENT)
                .profileStatus(ProfileStatus.ACTIVE)
                .isVerified(true)
                .school(admin.getSchool())
                .password(encodedPassword)
                .roles(Roles.STUDENT)
//                .profilePicture(imageUrl)
                .build();
        User savedUser = userRepository.save(newUser);

        Profile userProfile =  Profile.builder()
                .gender(userRequest.getGender())
                .religion(userRequest.getReligion())
                .city(userRequest.getCity())
                .state(userRequest.getState())
                .country(userRequest.getCountry())
                .studentGuardianOccupation(userRequest.getStudentGuardianOccupation())
                .studentGuardianOccupation(userRequest.getStudentGuardianOccupation())
                .studentGuardianName(userRequest.getStudentGuardianName())
                .studentGuardianPhoneNumber(userRequest.getStudentGuardianPhoneNumber())
                .uniqueRegistrationNumber(AccountUtils.generateStudentId())
                .address(userRequest.getAddress())
                .user(savedUser)
                .isVerified(true)
                .profileStatus(ProfileStatus.ACTIVE)
                .maritalStatus(userRequest.getMaritalStatus())
                .dateOfBirth(userRequest.getDateOfBirth())
                .admissionDate(userRequest.getAdmissionDate())
                .classBlock(classBlock)
//                .profilePicture(imageUrl)
                .phoneNumber(userRequest.getPhoneNumber())
                .build();
        Profile saveUserProfile = profileRepository.save(userProfile);

        // Increment class block and school student counts
        classBlock.setNumberOfStudents(classBlock.getNumberOfStudents() + 1);
        classBlockRepository.save(classBlock);

      //update the school population
        school.incrementActualNumberOfStudents();
        schoolRepository.save(school);

        //create wallet
        createWallet(saveUserProfile);

        sendRegistrationEmail(generatedPassword, savedUser, userProfile.getUniqueRegistrationNumber());

        AccountInfo accountInfo = buildAccountInfo(savedUser, userProfile);


        return new  UserResponse("200", "Student Successfully Created", accountInfo);
    }



//    private Map<String, Object> createModelWithData(UserRequestDto user) {
//        Map<String, Object> model = new HashMap<>();
//
//        // Add data to the model
//        model.put("name", user.getFirstName() + " " + user.getLastName());
//        model.put("email", user.getEmail());
//        model.put("username", user.getRegistrationNumber());
//        model.put("password", user.getPassword());
//
//        // You can add more data as needed for your email template
//
//        return model;
//    }


    @Override
    @Transactional
    public UserResponse createAdmin(UserRequestDto userRequest) throws MessagingException {

        validateUserRequest(userRequest);

        //        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
//        // Get the secure URL of the uploaded image from Cloudinary
//        String imageUrl = (String) uploadResult.get("secure_url");

        String generatedPassword = passwordGenerator.generateRandomPassword();

        String encodedPassword = passwordEncoder.encode(generatedPassword);

        User newUser = User.builder()
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .middleName(userRequest.getMiddleName())
                .email(userRequest.getEmail())
                .profileStatus(ProfileStatus.ACTIVE)
                .password(encodedPassword)
                .isVerified(true)
                .roles(Roles.ADMIN)
                .build();
        User savedUser = userRepository.save(newUser);


        Profile userProfile =  Profile.builder()
                .gender(userRequest.getGender())
                .dateOfBirth(userRequest.getDateOfBirth())
                .religion(userRequest.getReligion())
                .isVerified(true)
                .profileStatus(ProfileStatus.ACTIVE)
                .schoolGraduatedFrom(userRequest.getSchoolGraduatedFrom())
                .phoneNumber(userRequest.getPhoneNumber())
                .city(userRequest.getCity())
                .state(userRequest.getState())
                .country(userRequest.getCountry())
                .maritalStatus(userRequest.getMaritalStatus())
                .courseOfStudy(userRequest.getCourseOfStudy())
                .contractType(userRequest.getContractType())
                .academicQualification(userRequest.getAcademicQualification())
                .admissionDate(userRequest.getAdmissionDate())
                .uniqueRegistrationNumber(AccountUtils.generateStaffId())
                .address(userRequest.getAddress())
                .dateOfBirth(userRequest.getDateOfBirth())
                .user(savedUser)
                //                .profilePicture(imageUrl)
                .phoneNumber(userRequest.getPhoneNumber())
                .build();
        Profile saveUserProfile = profileRepository.save(userProfile);


        //create wallet
        createWallet(saveUserProfile);

        AccountInfo accountInfo = buildAccountInfo(savedUser, userProfile);

        return new  UserResponse("200", "Admin Successfully Created", accountInfo);
    }


    @Override
    @Transactional
    public UserResponse createStaff(UserRequestDto userRequest) throws MessagingException {

        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN)
                .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));
        StaffLevel staffLevel = staffLevelRepository.findById(userRequest.getStaffLevelId())
                .orElseThrow(() -> new CustomNotFoundException("Staff level not found"));

         School school = admin.getSchool();


        validateUserRequest(userRequest);

        String generatedPassword = passwordGenerator.generateRandomPassword();
        String encodedPassword = passwordEncoder.encode(generatedPassword);

        // Create new user
        User newUser = User.builder()
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .middleName(userRequest.getMiddleName())
                .email(userRequest.getEmail())
                .school(school)
                .profileStatus(ProfileStatus.ACTIVE)
                .password(encodedPassword)
                .isVerified(true)
                .roles(userRequest.getRole())
                .build();

        User savedUser = userRepository.save(newUser);

        Profile userProfile = buildStaffProfile(userRequest, savedUser, staffLevel);
        Profile savedProfile = profileRepository.save(userProfile);
        createWallet(savedProfile);
        sendStaffCreationEmail(savedUser, generatedPassword, userProfile.getUniqueRegistrationNumber());


        //update the school population
        school.incrementActualNumberOfStaff();
        schoolRepository.save(school);

        // Return the response
        AccountInfo accountInfo = buildAccountInfo(savedUser, userProfile);


        return new UserResponse("200", "Staff Successfully Created", accountInfo);
    }


    public LoginResponse loginUser(LoginRequest loginRequest) {

        try {

            Profile user = profileRepository.findByUniqueRegistrationNumber(loginRequest.getUsername())
                    .orElseThrow(() -> new CustomNotFoundException("Username not found"));

            Authentication authenticate = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUser().getEmail(), loginRequest.getPassword())
            );

            if (!authenticate.isAuthenticated()) {
                throw new UserPasswordMismatchException("Wrong email or password");
            }

            Optional<User> userDetails = userRepository.findByEmail(user.getUser().getEmail());
            // Check if the subscription has expired
            School school = userDetails.get().getSchool();
            if (school != null && !school.isSubscriptionValid()) {
                throw new SubscriptionExpiredException("Your subscription has expired. Please renew your subscription.");
            }

            SecurityContextHolder.getContext().setAuthentication(authenticate);
            String token = "Bearer " + jwtUtil.generateToken(user.getUser().getEmail(), userDetails.get().getSchool());

            // Create a UserDto object containing user details
            UserDto userDto = new UserDto();
            userDto.setFirstName(userDetails.get().getFirstName());
            userDto.setLastName(userDetails.get().getLastName());
            userDto.setEmail(userDetails.get().getEmail());

            return new LoginResponse(token, userDto);
        } catch (BadCredentialsException e) {
            // Handle the "Bad credentials" error here
            throw new AuthenticationFailedException("Wrong email or password");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public LoginResponse loginTeacher(LoginRequest loginRequest) {
        try {

            Profile user = profileRepository.findByUniqueRegistrationNumber(loginRequest.getUsername())
                    .orElseThrow(() -> new CustomNotFoundException("Username not found"));

            Authentication authenticate = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUser().getEmail(), loginRequest.getPassword())
            );

            if (!authenticate.isAuthenticated()) {
                throw new UserPasswordMismatchException("Wrong email or password");
            }

            Optional<User> userDetails = userRepository.findByEmail(user.getUser().getEmail());

            // Check if the subscription has expired
            School school = userDetails.get().getSchool();
            if (school != null && !school.isSubscriptionValid()) {
                throw new SubscriptionExpiredException("Your subscription has expired. Please renew your subscription.");
            }

            SecurityContextHolder.getContext().setAuthentication(authenticate);
            String token = "Bearer " + jwtUtil.generateToken(user.getUser().getEmail(), userDetails.get().getSchool());

            // Create a UserDto object containing user details
            UserDto userDto = new UserDto();
            userDto.setFirstName(userDetails.get().getFirstName());
            userDto.setLastName(userDetails.get().getLastName());
            userDto.setEmail(userDetails.get().getEmail());
            return new LoginResponse(token, userDto);
        } catch (BadCredentialsException e) {
            // Handle the "Bad credentials" error here
            throw new AuthenticationFailedException("Wrong email or password");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public LoginResponse loginAdmin(LoginRequest loginRequest) {
        try {

            Profile user = profileRepository.findByUniqueRegistrationNumber(loginRequest.getUsername())
                    .orElseThrow(() -> new CustomNotFoundException("Username not found"));

            Authentication authenticate = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUser().getEmail(), loginRequest.getPassword())
            );

            if (!authenticate.isAuthenticated()) {
                throw new UserPasswordMismatchException("Wrong email or password");
            }


            Optional<User> userDetails = userRepository.findByEmail(user.getUser().getEmail());
            if (userDetails.isEmpty()) {
                throw new UsernameNotFoundException("User not found");
            }

            // Check if the school exists
            School school = userDetails.get().getSchool();
            String token;
            if (school == null) {
                // Generate token with school as null
                token = "Bearer " + jwtUtil.generateToken(user.getUser().getEmail(), null);
            } else {
                // Generate token with school
                token = "Bearer " + jwtUtil.generateToken(user.getUser().getEmail(), school);
            }

            SecurityContextHolder.getContext().setAuthentication(authenticate);
            // Create a UserDto object containing user details
            UserDto userDto = new UserDto();
            userDto.setFirstName(userDetails.get().getFirstName());
            userDto.setLastName(userDetails.get().getLastName());
            userDto.setEmail(userDetails.get().getEmail());
            return new LoginResponse(token, userDto);
        } catch (BadCredentialsException e) {
            // Handle the "Bad credentials" error here
            throw new AuthenticationFailedException("Wrong email or password");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public UserResponse updateStudentClassLevel(Long studentId, Long newClassLevelId) {
        User existingUser = userRepository.findById(studentId)
                .orElseThrow(() -> new CustomNotFoundException("User not found with ID: " + studentId));

        ClassBlock newClassBlock = classBlockRepository.findById(newClassLevelId)
                .orElseThrow(() -> new BadRequestException("Class block not found with ID: " + newClassLevelId));


        ClassLevel classLevel = classLevelRepository.findByClassName(newClassBlock.getClassLevel().getClassName());
        if (classLevel == null) {
            throw new BadRequestException("Error: Class level not found for class " + newClassBlock.getCurrentStudentClassName());
        }

        newClassBlock.setNumberOfStudents(newClassBlock.getNumberOfStudents() + 1);
        classBlockRepository.save(newClassBlock);


        return modelMapper.map(existingUser, UserResponse.class);
    }

    @Transactional
    public UserResponse PromoteDueAcademicToPerformance(Long studentId, Long newClassLevelId) {
        // Fetch the existing user
        User existingUser = userRepository.findById(studentId)
                .orElseThrow(() -> new CustomNotFoundException("User not found with ID: " + studentId));

        Profile existingProfile = profileRepository.findByUser(existingUser)
                .orElseThrow(() -> new CustomNotFoundException("Profile not found with ID: " + studentId));

        // Find the class block by the new class level ID
        ClassBlock newClassBlock = classBlockRepository.findById(newClassLevelId)
                .orElseThrow(() -> new BadRequestException("Class block not found with ID: " + newClassLevelId));

        // Ensure that the class level is valid for the new class block
        ClassLevel classLevel = classLevelRepository.findByClassName(newClassBlock.getClassLevel().getClassName());
        if (classLevel == null) {
            throw new BadRequestException("Error: Class level not found for class " + newClassBlock.getCurrentStudentClassName());
        }

        // Decrement the number of students in the current class block of the user
        ClassBlock currentClassBlock = existingProfile.getClassBlock();
        if (currentClassBlock != null) {
            currentClassBlock.setNumberOfStudents(currentClassBlock.getNumberOfStudents() - 1);
            classBlockRepository.save(currentClassBlock);
        }

        // Update the student's class block to the new class block
        existingProfile.setClassBlock(newClassBlock);
        profileRepository.save(existingProfile);

        // Increment the number of students in the new class block
        newClassBlock.setNumberOfStudents(newClassBlock.getNumberOfStudents() + 1);
        classBlockRepository.save(newClassBlock);

        // Build and return the response
        return modelMapper.map(existingUser, UserResponse.class);
    }




    @Override
    public UserResponse editUserDetails(EditUserRequest editUserDto) {
        String email = SecurityConfig.getAuthenticatedUserEmail();   //why using this method and not autowired
        System.out.println(email+ "email");
        Object loggedInUsername1 = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println(loggedInUsername1 + "loggedInUsername1");

        User user = userRepository.findByEmail(email).orElseThrow(()-> new UsernameNotFoundException("User not found"));

        if (user == null){
            throw  new CustomNotFoundException( "User does not exist");
        }
        user.setFirstName(editUserDto.getFirstName());
        user.setLastName(editUserDto.getLastName());
        User updatedUser = userRepository.save(user);

        Profile userProfile =  Profile.builder()
                .gender(editUserDto.getGender())
                .dateOfBirth(editUserDto.getDateOfBirth())
                .religion(editUserDto.getReligion())
                .admissionDate(editUserDto.getAdmissionDate())
                .studentGuardianOccupation(editUserDto.getStudentGuardianOccupation())
                .studentGuardianOccupation(editUserDto.getStudentGuardianOccupation())
                .studentGuardianName(editUserDto.getStudentGuardianName())
                .studentGuardianPhoneNumber(editUserDto.getStudentGuardianPhoneNumber())
                .uniqueRegistrationNumber(AccountUtils.generateStudentId())
                .address(editUserDto.getAddress())
                .dateOfBirth(editUserDto.getDateOfBirth())
                .admissionDate(editUserDto.getAdmissionDate())
//                .profilePicture(imageUrl)
                .phoneNumber(editUserDto.getPhoneNumber())
                .build();
        profileRepository.save(userProfile);

        return modelMapper.map(user, UserResponse.class);
    }

    @Override
    public UserResponse forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {

        Optional<User> userOptional = userRepository.findByEmail(forgotPasswordRequest.getEmail());

        if (userOptional.isEmpty()) {
            throw new CustomNotFoundException("User with provided Email not found");
        }

        User user = userOptional.get();
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(forgotPasswordRequest.getEmail());


        // Generate a new token
//        String token = new JwtUtil().generateToken(user.getEmail(), user.getSchool());

        // Check if the user already has a PasswordResetToken
        PasswordResetToken existingToken = passwordResetTokenRepository.findByUser(user);

        if (existingToken != null) {
            // Update the existing token
//            existingToken.setResetToken(token);
//            existingToken.setExpirationDate(new Date()); // Update expiration date if needed
        } else {
            // Create a new PasswordResetToken if none exists
            PasswordResetToken passwordResetTokenEntity = new PasswordResetToken();
//            passwordResetTokenEntity.setResetToken(token);
            passwordResetTokenEntity.setUser(user);
            passwordResetTokenRepository.save(passwordResetTokenEntity);
        }


        Map<String, Object> model = new HashMap<>();
//        model.put("passwordResetLink", "http://localhost:8080/api/users/resetPassword?token=" + token);

        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(forgotPasswordRequest.getEmail())
                .subject("PASSWORD RESET LINK")
                .templateName("password-reset-email")  // Thymeleaf template name
                .model(model)  // Pass the model
                .build();

        try {
            emailService.sendEmailWithThymeleaf(emailDetails);  // Use a new method for Thymeleaf email sending
        } catch (Exception e) {
            // Handle the email sending error here
            throw new EmailSendingException("Failed to send the password reset email");
        }

        return modelMapper.map(user, UserResponse.class);
    }

    @Override
    @Transactional
    public UserResponse resetPassword(PasswordResetRequest passwordRequest, String token) {
        // Validate new password and confirm password match
        if (!passwordRequest.getNewPassword().equals(passwordRequest.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        // Validate password length
        if (passwordRequest.getNewPassword().length() < 8) {
            throw new BadRequestException("Password is too short, must be at least 8 characters");
        }

        // Extract email and find user
        String email = jwtUtil.extractUsername(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Check if the token is expired
        if (jwtUtil.isTokenExpired(token)) {
            throw new TokenExpiredException("Token has expired");
        }

        // Update user's password
        user.setPassword(passwordEncoder.encode(passwordRequest.getNewPassword()));
        userRepository.save(user);

        // Delete the used reset token
        passwordResetTokenRepository.deleteByResetToken(token);

        // Map and return response
        return modelMapper.map(user, UserResponse.class);
    }

    @Override
    public UserResponse updatePassword (ChangePasswordRequest changePasswordRequest) {
        String email = SecurityConfig.getAuthenticatedUserEmail(); // please confirm
        String oldPassword = changePasswordRequest.getOldPassword();
        String newPassword = changePasswordRequest.getNewPassword();
        String confirmPassword = changePasswordRequest.getConfirmPassword();

        Optional<User> optionalUsers = userRepository.findByEmail(email);

        if(optionalUsers.isPresent()){
            User users = optionalUsers.get();
            String encodedPassword = users.getPassword();
            boolean isPasswordAMatch = passwordEncoder.matches(oldPassword, encodedPassword);

            if(!isPasswordAMatch) {
                throw  new BadRequestException("Old Password does not match");
            }

            if(changePasswordRequest.getNewPassword().length() < 8 || changePasswordRequest.getConfirmPassword().length() < 8 ){
                throw new BadRequestException("Error: Password is too short, should be minimum of 8 character long");
            }

            boolean isPasswordEquals = newPassword.equals(confirmPassword);

            if(!isPasswordEquals){
                throw  new BadRequestException("New Password does not match confirm password");
            }

            users.setPassword(passwordEncoder.encode(newPassword));

            userRepository.save(users);
        }

        return modelMapper.map(optionalUsers.get(), UserResponse.class);
    }

    @Override
    public UserResponse getUser() {
        try {
            String loggedInEmail = SecurityContextHolder.getContext().getAuthentication().getName();

            if ("anonymousUser".equals(loggedInEmail)) {
                throw new ResourceNotFoundException("This user does not exist");
            }

            User user = userRepository.findByEmail(loggedInEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            return modelMapper.map(user, UserResponse.class);

        } catch (Exception e) {
            throw new CustomInternalServerException("Internal server error "+e);
        }
    }
    public ResponseEntity<Void> deleteUser(Long userId) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN)
                    .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));

            if (userRepository.existsById(userId)) {
                userRepository.deleteById(userId);
                return ResponseEntity.noContent().build(); // User deleted successfully
            } else {
                return ResponseEntity.notFound().build(); // User not found
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public UserResponse generateIdCard(String uniqueRegistrationNumber) {
        try{
            Optional<Profile> user = profileRepository.findByUniqueRegistrationNumber(uniqueRegistrationNumber);
            if(user.isPresent()){
                String schoolName = user.get().getUser().getSchool().getSchoolName();

                String idCard = idCardService.generateIdCard(schoolName, user.get().getUser(), user.get() );
                System.out.println(idCard);
                return modelMapper.map(user.get(), UserResponse.class);
            }else {
                throw new ResourceNotFoundException("User not found");
            }
        }catch (Exception e){
            throw new RuntimeException("Error generating ID card");

        }
    }


    public Page<UserResponse> getAllStudentsFilteredAndPaginated(
            Long classId,
            Long subClassId,
            Long academicYearId,
            String uniqueRegistrationNumber,
            String firstName,
            int page,
            int size,
            String sortBy) {

        // Create Pageable object for pagination
        Pageable paging = PageRequest.of(page, size, Sort.by(sortBy).ascending());

        // Resolve optional filters
        ClassBlock subClass = subClassId != null ?
                classBlockRepository.findById(subClassId)
                        .orElseThrow(() -> new CustomNotFoundException("Subclass not found")) :
                null;

        ClassLevel classLevel = classId != null ?
                classLevelRepository.findById(classId)
                        .orElseThrow(() -> new CustomNotFoundException("Class not found")) :
                null;

        AcademicSession academicYear = academicYearId != null ?
                academicSessionRepository.findById(academicYearId)
                        .orElseThrow(() -> new CustomNotFoundException("Academic year not found")) :
                null;

        // Fetch students with optional filters
        Page<Profile> students = profileRepository.findAllWithFilters(
                subClass,
                classLevel,
                academicYear,
                uniqueRegistrationNumber,
                firstName,
                paging);

        return students.map(element -> modelMapper.map(element, UserResponse.class));
    }

    private boolean isSubscriptionExpired(School school) {
        LocalDateTime expiryDate = school.getSubscriptionExpiryDate();
        return expiryDate != null && expiryDate.isBefore(LocalDateTime.now());
    }



    private boolean isValidEmail(String email) {
        String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";

        // Compile the ReGex
        Pattern p = Pattern.compile(regex);
        if (email == null) {
            throw new BadRequestException("Error: Email cannot be null");
        }
        Matcher m = p.matcher(email);
        return m.matches();
    }
    private boolean existsByMail(String email) {
        return userRepository.existsByEmail(email);


    }
    public String updateUserStatus(Long userId, ProfileStatus newStatus, LocalDate suspensionEndDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found for user ID: " + userId));
        // Fetch the user profile
        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("Profile not found for user ID: " + userId));

        // Set the new profile status (it can be any of the restricted statuses)
        profile.setProfileStatus(newStatus);

        // Optionally set the suspension end date if the status requires it (e.g., for suspension)
        if (newStatus == ProfileStatus.SUSPENDED && suspensionEndDate != null) {
            profile.setSuspensionEndDate(suspensionEndDate);
        } else {
            profile.setSuspensionEndDate(null); // Clear the end date if the status is not suspension
        }

        // Save the updated profile
        profileRepository.save(profile);

        return "User status updated successfully.";
    }

    public Page<UserProfileResponse> getProfilesByRoleAndStatus(String role, String status, int page, int size) {
        // Convert role and status strings to enum
        Roles roleEnum = Roles.valueOf(role);  // Converts to enum
        ProfileStatus statusEnum = ProfileStatus.valueOf(status);  // Converts to enum

        // Create pageable object
        Pageable pageable = PageRequest.of(page, size);

        // Query the repository to get the profiles
        return profileRepository.findProfilesByRoleAndStatus(roleEnum, statusEnum, pageable);
    }


    private void validateUserRequest(UserRequestDto userRequest) {
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new UserAlreadyExistException("Email already exists");
        }

        if (!AccountUtils.isValidEmail(userRequest.getEmail())) {
            throw new BadRequestException("Invalid email address");
        }

    }

    private void sendRegistrationEmail(String generatedPassword, User savedUser, String regNo) throws MessagingException {
        Map<String, Object> model = new HashMap<>();
        model.put("name", savedUser.getFirstName() + " " + savedUser.getLastName());
        model.put("username", regNo);
        model.put("password", generatedPassword);

        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(savedUser.getEmail())
                .subject("Successful Registration")
                .templateName("email-template") // Thymeleaf template name
                .model(model)
                .build();
        emailService.sendHtmlEmail(emailDetails);
    }


    private Profile buildStaffProfile(UserRequestDto userRequest, User savedUser, StaffLevel staffLevel) {
        Profile.ProfileBuilder profileBuilder = Profile.builder()
                .gender(userRequest.getGender())
                .isVerified(true)
                .staffLevel(staffLevel)
                .profileStatus(ProfileStatus.ACTIVE)
                .dateOfBirth(userRequest.getDateOfBirth())
                .courseOfStudy(userRequest.getCourseOfStudy())
                .classOfDegree(userRequest.getClassOfDegree())
                .admissionDate(userRequest.getAdmissionDate())
                .city(userRequest.getCity())
                .state(userRequest.getState())
                .country(userRequest.getCountry())
                .contractType(userRequest.getContractType())
                .maritalStatus(userRequest.getMaritalStatus())
                .schoolGraduatedFrom(userRequest.getSchoolGraduatedFrom())
                .academicQualification(userRequest.getAcademicQualification())
                .religion(userRequest.getReligion())
                .uniqueRegistrationNumber(AccountUtils.generateStaffId())
                .address(userRequest.getAddress())
                .phoneNumber(userRequest.getPhoneNumber())
                .user(savedUser);

        if (userRequest.getClassFormTeacherId() != null) {
            ClassBlock classBlock = classBlockRepository.findById(userRequest.getClassFormTeacherId())
                    .orElseThrow(() -> new NotFoundException("Class not found with ID: " + userRequest.getClassFormTeacherId()));
            profileBuilder.classFormTeacher(classBlock);
        }

        if (userRequest.getSubjectAssignedId() != null) {
            Subject subject = subjectRepository.findById(userRequest.getSubjectAssignedId())
                    .orElseThrow(() -> new NotFoundException("Subject not found with ID: " + userRequest.getSubjectAssignedId()));
            profileBuilder.subjectAssigned(subject);
        }

        return profileBuilder.build();
    }

    private void createWallet(Profile userProfile) {
        Wallet userWallet = new Wallet();
        userWallet.setBalance(BigDecimal.ZERO);
        userWallet.setTotalMoneySent(BigDecimal.ZERO);
        userWallet.setUserProfile(userProfile);
        walletRepository.save(userWallet);
    }

    private void sendStaffCreationEmail(User savedUser, String generatedPassword, String regNo) throws MessagingException {

        Map<String, Object> model = new HashMap<>();
        model.put("name", savedUser.getFirstName() + " " + savedUser.getLastName());
        model.put("username", regNo);
        model.put("password", generatedPassword);

        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(savedUser.getEmail())
                .subject("ACCOUNT CREATION")
                .templateName("email-template-teachers")
                .model(model)
                .build();
        emailService.sendHtmlEmail(emailDetails);

    }

    private AccountInfo buildAccountInfo(User savedUser, Profile userProfile) {
        return AccountInfo.builder()
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .email(savedUser.getEmail())
                .gender(userProfile.getGender())
                .build();
    }





}


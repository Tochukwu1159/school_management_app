package examination.teacherAndStudents.service.serviceImpl;
import com.cloudinary.Cloudinary;
import examination.teacherAndStudents.Security.CustomUserDetailService;
import examination.teacherAndStudents.Security.JwtUtil;
import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.*;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.EmailService;
import examination.teacherAndStudents.service.UserService;
import examination.teacherAndStudents.utils.AccountUtils;
import examination.teacherAndStudents.utils.MaritalStatus;
import examination.teacherAndStudents.utils.ProfileStatus;
import examination.teacherAndStudents.utils.Roles;
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
    private final  HttpServletRequest httpServletRequest;
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
    private final AcademicSessionRepository academicSessionRepository;

    private  final  AccountUtils accountUtils;
    private final SubjectRepository subjectRepository;
    private final SchoolRepository schoolRepository;

    @Override
    public UserResponse createStudent(UserRequestDto userRequest) throws MessagingException {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);

        if (admin == null) {
            throw new AuthenticationFailedException("Please login as an Admin");
        }

        Optional<User> userDetails = userRepository.findByEmail(email);


        if (userRepository.existsByEmail(userRequest.getEmail())) {

           throw new UserAlreadyExistException("Email already exist");

        }
        if (!AccountUtils.validatePassword(userRequest.getPassword(), userRequest.getConfirmPassword())){
            throw new UserPasswordMismatchException("Password does not match");

    }
        if(existsByMail(userRequest.getEmail())){
            throw new BadRequestException
                    ("Error: Email is already taken!");
        }

        if(!isValidEmail(userRequest.getEmail())){
            throw new BadRequestException("Error: Email must be valid");
        }

        if(userRequest.getPassword().length() < 8 || userRequest.getConfirmPassword().length() < 8 ){
            throw new BadRequestException("Password is too short, should be minimum of 8 character long");
        }
        Optional<ClassBlock> studentClassBlock = classBlockRepository.findById(userRequest.getClassAssignedId());
        ClassLevel classLevel = classLevelRepository.findByClassName(studentClassBlock.get().getClassLevel().getClassName());
        if (classLevel == null) {
            throw new BadRequestException("Error: Class level not found ");
        }
//        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
//        // Get the secure URL of the uploaded image from Cloudinary
//        String imageUrl = (String) uploadResult.get("secure_url");

        User newUser = User.builder()
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .middleName(userRequest.getMiddleName())
                .school(userDetails.get().getSchool())
                .email(userRequest.getEmail())
                .roles(Roles.STUDENT)
                .isVerified(true)
                .school(userDetails.get().getSchool())
                .password(passwordEncoder.encode(userRequest.getPassword()))
                .roles(Roles.STUDENT)
//                .profilePicture(imageUrl)
                .build();
        User savedUser = userRepository.save(newUser);

        Profile userProfile =  Profile.builder()
                .gender(userRequest.getGender())
                .dateOfBirth(userRequest.getDateOfBirth())
                .religion(userRequest.getReligion())
                .admissionDate(userRequest.getAdmissionDate())
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
                .classBlock(studentClassBlock.get())
//                .profilePicture(imageUrl)
                .phoneNumber(userRequest.getPhoneNumber())
                .build();
        Profile saveUserProfile = profileRepository.save(userProfile);

        studentClassBlock.get().setNumberOfStudents(studentClassBlock.get().getNumberOfStudents() + 1);
        classBlockRepository.save(studentClassBlock.get());
        //create wallet
        Wallet userWallet = new Wallet();
        userWallet.setBalance(BigDecimal.ZERO);
        userWallet.setTotalMoneySent(BigDecimal.ZERO);
        userWallet.setUserProfile(saveUserProfile);
        walletRepository.save(userWallet);

        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(savedUser.getEmail())
                .subject("SUCCESSFULLY REGISTRATION")
                .templateName("email-template")  // Name of your Thymeleaf template
                .model(createModelWithData(userRequest))
                .build();
        emailService.sendHtmlEmail(emailDetails);

        AccountInfo accountInfo = AccountInfo.builder().
                firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .email(savedUser.getEmail())
                .gender(savedUser.getEmail()).build();


        return new  UserResponse("200", "Student Successfully Created", accountInfo);
    }



    private Map<String, Object> createModelWithData(UserRequestDto user) {
        Map<String, Object> model = new HashMap<>();

        // Add data to the model
        model.put("name", user.getFirstName() + " " + user.getLastName());
        model.put("email", user.getEmail());
        model.put("password", user.getPassword());

        // You can add more data as needed for your email template

        return model;
    }


    @Override
    public UserResponse createAdmin(UserRequestDto userRequest) throws MessagingException {

        School school = schoolRepository.findById(userRequest.getSchoolId())
                .orElseThrow(() -> new CustomNotFoundException("School not found or school id not exist"));

        if (userRepository.existsByEmail(userRequest.getEmail())) {

            throw new UserAlreadyExistException("User with email already exist");

        }
        if (!AccountUtils.validatePassword(userRequest.getPassword(), userRequest.getConfirmPassword())){
            throw new UserPasswordMismatchException("Password does not match");

        }
        if(existsByMail(userRequest.getEmail())){
            throw new BadRequestException("Error: Email is already taken!");
        }

        if(!isValidEmail(userRequest.getEmail())){
            throw new BadRequestException("Error: Email must be valid");
        }

        if(userRequest.getPassword().length() < 8 || userRequest.getConfirmPassword().length() < 8 ){
            throw new BadRequestException("Password is too short, should be minimum of 8 character longt");
        }

        //        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
//        // Get the secure URL of the uploaded image from Cloudinary
//        String imageUrl = (String) uploadResult.get("secure_url");


        User newUser = User.builder()
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .middleName(userRequest.getMiddleName())
                .email(userRequest.getEmail())
                .school(school)
                .password(passwordEncoder.encode(userRequest.getPassword()))
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
                .maritalStatus(userRequest.getMaritalStatus())
                .courseOfStudy(userRequest.getCourseOfStudy())
                .contractType(userRequest.getContractType())
                .academicQualification(userRequest.getAcademicQualification())
                .admissionDate(userRequest.getAdmissionDate())
                .uniqueRegistrationNumber(AccountUtils.generateTeacherId())
                .address(userRequest.getAddress())
                .dateOfBirth(userRequest.getDateOfBirth())
                .user(savedUser)
                //                .profilePicture(imageUrl)
                .phoneNumber(userRequest.getPhoneNumber())
                .build();
        Profile saveUserProfile = profileRepository.save(userProfile);


        //create wallet
        Wallet userWallet = new Wallet();
        userWallet.setBalance(BigDecimal.ZERO);
        userWallet.setTotalMoneySent(BigDecimal.ZERO);
        userWallet.setUserProfile(saveUserProfile);
        walletRepository.save(userWallet);

        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(savedUser.getEmail())
                .subject("ACCOUNT CREATION")
                .templateName("email-template-admin")
                .model(Map.of("name", savedUser.getFirstName() + " " + savedUser.getLastName()))
                .build();
        emailService.sendHtmlEmail(emailDetails);

        AccountInfo accountInfo = AccountInfo.builder().
                firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .email(savedUser.getEmail())
                .gender(savedUser.getEmail()).build();


        return new  UserResponse("200", "Admin Successfully Created", accountInfo);
    }


    @Override
    public UserResponse createTeacher(UserRequestDto userRequest) throws MessagingException {

        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);

        if (admin == null) {
            throw new AuthenticationFailedException("Please login as an Admin");
        }

        Optional<User> userDetails = userRepository.findByEmail(email);

        School school = schoolRepository.findById(userRequest.getSchoolId())
                .orElseThrow(() -> new CustomNotFoundException("Please login as a student"));

        // Check if ClassFormTeacherId is provided before fetching ClassBlock
        if (userRequest.getClassFormTeacherId() != null) {
            ClassBlock classBlockAssigned = classBlockRepository.findById(userRequest.getClassFormTeacherId())
                    .orElseThrow(() -> new NotFoundException("Class not found with ID: " + userRequest.getClassFormTeacherId()));
        }

        // Check if SubjectAssignedId is provided before fetching Subject
        if (userRequest.getSubjectAssignedId() != null) {
            Subject assignedSubject = subjectRepository.findById(userRequest.getSubjectAssignedId())
                    .orElseThrow(() -> new NotFoundException("Subject not found with ID: " + userRequest.getSubjectAssignedId()));
        }

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new UserAlreadyExistException("User with email already exists");
        }

        if (!AccountUtils.validatePassword(userRequest.getPassword(), userRequest.getConfirmPassword())) {
            throw new UserPasswordMismatchException("Password does not match");
        }

        if (existsByMail(userRequest.getEmail())) {
            throw new BadRequestException("Error: Email is already taken!");
        }

        if (!isValidEmail(userRequest.getEmail())) {
            throw new BadRequestException("Error: Email must be valid");
        }

        if (userRequest.getPassword().length() < 8 || userRequest.getConfirmPassword().length() < 8) {
            throw new BadRequestException("Password is too short, should be a minimum of 8 characters long");
        }

        // Create new user
        User newUser = User.builder()
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .school(userDetails.get().getSchool())
                .middleName(userRequest.getMiddleName())
                .email(userRequest.getEmail())
                .school(school)
                .password(passwordEncoder.encode(userRequest.getPassword()))
                .isVerified(true)
                .roles(Roles.TEACHER)
                .build();

        User savedUser = userRepository.save(newUser);

        // Create Profile with the condition that ClassBlock and Subject are assigned only if they are provided
        Profile userProfile = Profile.builder()
                .gender(userRequest.getGender())
                .isVerified(true)
                .profileStatus(ProfileStatus.ACTIVE)
                .dateOfBirth(userRequest.getDateOfBirth())
                .courseOfStudy(userRequest.getCourseOfStudy())
                .classOfDegree(userRequest.getClassOfDegree())
                .admissionDate(userRequest.getAdmissionDate())
                .contractType(userRequest.getContractType())
                .dateOfBirth(userRequest.getDateOfBirth())
                .salary(userRequest.getSalary())
                .schoolGraduatedFrom(userRequest.getSchoolGraduatedFrom())
                .academicQualification(userRequest.getAcademicQualification())
                .religion(userRequest.getReligion())
                .admissionDate(userRequest.getAdmissionDate())
                .uniqueRegistrationNumber(AccountUtils.generateTeacherId())
                .address(userRequest.getAddress())
                .phoneNumber(userRequest.getPhoneNumber())
                .user(savedUser)
                .build();

        // Only assign class and subject if the IDs are provided
        if (userRequest.getClassFormTeacherId() != null) {
            ClassBlock classBlockAssigned = classBlockRepository.findById(userRequest.getClassFormTeacherId())
                    .orElseThrow(() -> new NotFoundException("Class not found with ID: " + userRequest.getClassFormTeacherId()));
            userProfile.setClassFormTeacher(classBlockAssigned);
        }

        if (userRequest.getSubjectAssignedId() != null) {
            Subject assignedSubject = subjectRepository.findById(userRequest.getSubjectAssignedId())
                    .orElseThrow(() -> new NotFoundException("Subject not found with ID: " + userRequest.getSubjectAssignedId()));
            userProfile.setSubjectAssigned(assignedSubject);
        }

        Profile saveUserProfile = profileRepository.save(userProfile);

        // Create wallet
        Wallet userWallet = new Wallet();
        userWallet.setBalance(BigDecimal.ZERO);
        userWallet.setTotalMoneySent(BigDecimal.ZERO);
        userWallet.setUserProfile(saveUserProfile);
        walletRepository.save(userWallet);

        // Send email
        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(savedUser.getEmail())
                .subject("ACCOUNT CREATION")
                .templateName("email-template-teachers")
                .model(Map.of("name", savedUser.getFirstName() + " " + savedUser.getLastName()))
                .build();

        emailService.sendHtmlEmail(emailDetails);

        // Return the response
        AccountInfo accountInfo = AccountInfo.builder()
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .email(savedUser.getEmail())
                .gender(userProfile.getGender())
                .build();

        return new UserResponse("200", "Teacher Successfully Created", accountInfo);
    }


    public LoginResponse loginUser(LoginRequest loginRequest) {
        try {
            Authentication authenticate = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            if (!authenticate.isAuthenticated()) {
                throw new UserPasswordMismatchException("Wrong email or password");
            }
            Optional<User> userDetails = userRepository.findByEmail(loginRequest.getEmail());

            // Check if the subscription has expired
            School school = userDetails.get().getSchool();
            if (school != null && !school.isSubscriptionValid()) {
                throw new SubscriptionExpiredException("Your subscription has expired. Please renew your subscription.");
            }

            SecurityContextHolder.getContext().setAuthentication(authenticate);
            String token = "Bearer " + jwtUtil.generateToken(loginRequest.getEmail(), userDetails.get().getSchool().getSubscriptionKey());

            // Create a UserDto object containing user details
            UserDto userDto = new UserDto();
            userDto.setFirstName(userDetails.get().getFirstName());
            userDto.setLastName(userDetails.get().getLastName());
            userDto.setEmail(userDetails.get().getEmail());

            return new LoginResponse(token, userDto);
        } catch (BadCredentialsException e) {
            // Handle the "Bad credentials" error here
            throw new AuthenticationFailedException("Wrong email or password");
        }
    }

    public LoginResponse loginTeacher(LoginRequest loginRequest) {
        try {
            Authentication authenticate = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            if (!authenticate.isAuthenticated()) {
                throw new UserPasswordMismatchException("Wrong email or password");
            }

            Optional<User> userDetails = userRepository.findByEmail(loginRequest.getEmail());

            // Check if the subscription has expired
            School school = userDetails.get().getSchool();
            if (school != null && !school.isSubscriptionValid()) {
                throw new SubscriptionExpiredException("Your subscription has expired. Please renew your subscription.");
            }

            SecurityContextHolder.getContext().setAuthentication(authenticate);
            String token = "Bearer " + jwtUtil.generateToken(loginRequest.getEmail(), userDetails.get().getSchool().getSubscriptionKey());

            // Create a UserDto object containing user details
            UserDto userDto = new UserDto();
            userDto.setFirstName(userDetails.get().getFirstName());
            userDto.setLastName(userDetails.get().getLastName());
            userDto.setEmail(userDetails.get().getEmail());
            return new LoginResponse(token, userDto);
        } catch (BadCredentialsException e) {
            // Handle the "Bad credentials" error here
            throw new AuthenticationFailedException("Wrong email or password");
        }
    }

    public LoginResponse loginAdmin(LoginRequest loginRequest) {
        try {
            Authentication authenticate = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            if (!authenticate.isAuthenticated()) {
                throw new UserPasswordMismatchException("Wrong email or password");
            }


            Optional<User> userDetails = userRepository.findByEmail(loginRequest.getEmail());


            // Check if the subscription has expired
            School school = userDetails.get().getSchool();
            if(school.getSubscriptionExpiryDate() == null){
                throw new SubscriptionExpiredException("Not subscribe yet. Please  subscribe to enjoy the services.");
            }
            if (school != null && !school.isSubscriptionValid()) {
                throw new SubscriptionExpiredException("Your subscription has expired. Please renew your subscription.");
            }

            SecurityContextHolder.getContext().setAuthentication(authenticate);
            String token = "Bearer " + jwtUtil.generateToken(loginRequest.getEmail(), userDetails.get().getSchool().getSubscriptionKey());

            // Create a UserDto object containing user details
            UserDto userDto = new UserDto();
            userDto.setFirstName(userDetails.get().getFirstName());
            userDto.setLastName(userDetails.get().getLastName());
            userDto.setEmail(userDetails.get().getEmail());
            return new LoginResponse(token, userDto);
        } catch (BadCredentialsException e) {
            // Handle the "Bad credentials" error here
            throw new AuthenticationFailedException("Wrong email or password");
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
        String token = new JwtUtil().generateToken(user.getEmail(), user.getSchool().getSubscriptionKey());

        // Check if the user already has a PasswordResetToken
        PasswordResetToken existingToken = passwordResetTokenRepository.findByUser(user);

        if (existingToken != null) {
            // Update the existing token
            existingToken.setResetToken(token);
//            existingToken.setExpirationDate(new Date()); // Update expiration date if needed
        } else {
            // Create a new PasswordResetToken if none exists
            PasswordResetToken passwordResetTokenEntity = new PasswordResetToken();
            passwordResetTokenEntity.setResetToken(token);
            passwordResetTokenEntity.setUser(user);
            passwordResetTokenRepository.save(passwordResetTokenEntity);
        }


        Map<String, Object> model = new HashMap<>();
        model.put("passwordResetLink", "http://localhost:8080/api/users/resetPassword?token=" + token);

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
        if (!passwordRequest.getNewPassword().equals(passwordRequest.getConfirmPassword())) {
            throw new CustomNotFoundException("Password do not match");
        }

        String email = jwtUtil.extractUsername(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        if (passwordRequest.getNewPassword().length() < 8 || passwordRequest.getConfirmPassword().length() < 8) {
            throw new BadRequestException("Error: Password is too short");
        }

        // Check if the token has expired
//        Date tokenExpirationDate = jwtUtil. extractExpiration(token);
//        Date currentDate = new Date();

//        if (tokenExpirationDate != null && currentDate.after(tokenExpirationDate)) {
            if(jwtUtil.isTokenExpired(token)){
            throw new TokenExpiredException("Token has expired");
        }

        user.setPassword(passwordEncoder.encode(passwordRequest.getNewPassword()));
        userRepository.save(user);

        passwordResetTokenRepository.deleteByResetToken(token);

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

//    @Override
//        public AllUserResponse getAllUsers() {
//            try {
//                List<User> userList = userRepository.findAll();
//
//                List<AccountInfo> accountInfoList = userList.stream()
//                        .map(user -> new AccountInfo(
//                                user.getFirstName(),
//                                user.getLastName(),
//                                user.getEmail(),
//                                user.getPhoneNumber(),
//                                user.getUniqueRegistrationNumber(),
//                                user.getAge(),
//                                user.getStudentGuardianPhoneNumber(),
//                                user.getClassAssigned(),
//                                user.getPhoneNumber(),
//                                user.getGender(),
//                                user.getAddress(),
//                                user.getSubjectAssigned(),
//                                user.getAcademicQualification(),
//                                user.getUniqueRegistrationNumber()
//                        ))
//                        .collect(Collectors.toList());
//
//                return AllUserResponse.builder()
//                        .responseCode(AccountUtils.FETCH_ALL_USERS_SUCCESSFUL_CODE)
//                        .responseMessage(AccountUtils.FETCH_ALL_USERS_SUCCESSFUL_MESSAGE)
//                        .accountInfo(accountInfoList)
//                        .build();
//            } catch (Exception e) {
//                // Log the exception for further analysis
//                e.printStackTrace();
//                return AllUserResponse.builder()
//                        .responseCode(AccountUtils.INTERNAL_SERVER_ERROR_CODE)
//                        .responseMessage(AccountUtils.INTERNAL_SERVER_ERROR_MESSAGE)
//                        .accountInfo(Collections.emptyList()) // Return an empty list in case of an error
//                        .build();
//            }
//        }

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
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);

            if (!admin.getIsVerified()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

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
    public UserResponse geenerateIdCard(String uniqueRegistrationNumber) {
        try{
            Optional<Profile> user = profileRepository.findByUniqueRegistrationNumber(uniqueRegistrationNumber);
            if(user.isPresent()){
                return modelMapper.map(user.get(), UserResponse.class);
            }else {
                throw new ResourceNotFoundException("User not found");
            }

        }catch (Exception e){
            throw new RuntimeException("Error generating ID card");

        }
    }
    public Page<UserResponse> getAllStudentsFilteredAndPaginated(
            Long classCategoryId,
            Long subClassId,
            Long academicYearId,
            int page,
            int size,
            String sortBy
    ) {
        Optional<AcademicSession> academicYearOptional = academicSessionRepository.findById(academicYearId);
        Optional<ClassLevel> studentClassLevelOptional = classLevelRepository.findById(classCategoryId);
        Optional<ClassBlock> subClassOptional = classBlockRepository.findById(subClassId);


        AcademicSession academicYear = academicYearOptional.orElseThrow(() -> new CustomNotFoundException("Academic year not found"));
        ClassLevel studentClassLevel = studentClassLevelOptional.orElseThrow(() -> new CustomNotFoundException("Student class level not found"));
        ClassBlock subClass = subClassOptional.orElseThrow(() -> new CustomNotFoundException("Subclass not found"));


        // Create Pageable object for pagination
        Pageable paging = PageRequest.of(page, size, Sort.by(sortBy).ascending());

        // Fetch students based on filters
        Page<Profile> students = profileRepository.findAllByClassBlock(
                subClass, paging);

        return students.map((element) -> modelMapper.map(element, UserResponse.class));
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





}


package examination.teacherAndStudents.service.serviceImpl;
import com.cloudinary.Cloudinary;
import com.fasterxml.jackson.core.JsonProcessingException;
import examination.teacherAndStudents.Security.CustomUserDetailService;
import examination.teacherAndStudents.Security.JwtUtil;
import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.entity.EmergencyContact;
import examination.teacherAndStudents.error_handler.*;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.EmailService;
import examination.teacherAndStudents.service.FeeService;
import examination.teacherAndStudents.service.UserService;
import examination.teacherAndStudents.templateService.IdCardService;
import examination.teacherAndStudents.utils.*;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
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
    private final FeeService feeStructureService;
    private final AdmissionApplicationRepository admissionApplicationRepository;
    private final DocumentRepository documentRepository;
    private final ReferralRepository referralRepository;
    private final SessionClassRepository sessionClassRepository;


    @Override
    @Transactional
    public UserResponse createStudent(UserRequestDto userRequest) throws MessagingException {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));

        School school = admin.getSchool();
        validateUserRequest(userRequest);

        // Validate class assignment (optional)
        SessionClass sessionClass = null;
        if (userRequest.getClassAssignedId() != null) {
            ClassBlock classBlock = classBlockRepository.findByIdAndSchoolId(userRequest.getClassAssignedId(), admin.getSchool().getId())
                    .orElseThrow(() -> new BadRequestException("Class block not found with ID: " + userRequest.getClassAssignedId()));

            // Find the current active AcademicSession
            AcademicSession currentSession = academicSessionRepository.findByStatusAndSchoolId(
                            SessionStatus.ACTIVE, school.getId())
                    .orElseThrow(() -> new CustomNotFoundException("No active academic session found for school ID: " + school.getId()));

            // Find or create SessionClass
            sessionClass = sessionClassRepository.findBySessionIdAndClassBlockId(
                            currentSession.getId(), classBlock.getId())
                    .orElseGet(() -> {
                        SessionClass newSessionClass = SessionClass.builder()
                                .academicSession(currentSession)
                                .classBlock(classBlock)
                                .profiles(new HashSet<>())
                                .numberOfProfiles(0)
                                .build();
                        return sessionClassRepository.save(newSessionClass);
                    });
        } else {
            log.info("No class assigned to student during creation");
        }

        // Generate credentials and referral
        String generatedPassword = passwordGenerator.generateRandomPassword();
        String encodedPassword = passwordEncoder.encode(generatedPassword);
        String referralCode = generateReferralCode(userRequest.getFirstName(), userRequest.getLastName());
        String referralLink = generateReferralLink(referralCode);

        // Create and save User
        User newUser = User.builder()
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .middleName(userRequest.getMiddleName())
                .school(school)
                .email(userRequest.getEmail())
                .roles(Collections.singleton(Roles.STUDENT))
                .profileStatus(ProfileStatus.ACTIVE)
                .isVerified(true)
                .password(encodedPassword)
                .build();
        User savedUser = userRepository.save(newUser);

        // Build Profile
        Set<EmergencyContact> emergencyContacts = buildEmergencyContacts(userRequest.getEmergencyContacts());
        Set<Address> addresses = buildAddressesFromDto(userRequest.getAddresses());

        Profile userProfile = Profile.builder()
                .gender(userRequest.getGender())
                .religion(userRequest.getReligion())
                .addresses(addresses)
                .emergencyContacts(emergencyContacts)
                .referralLink(referralLink)
                .referralCode(referralCode)
                .studentGuardianOccupation(userRequest.getStudentGuardianOccupation())
                .studentGuardianName(userRequest.getStudentGuardianName())
                .studentGuardianPhoneNumber(userRequest.getStudentGuardianPhoneNumber())
                .uniqueRegistrationNumber(AccountUtils.generateStudentId(school.getSchoolCode()))
                .user(savedUser)
                .sessionClass(sessionClass)
                .isVerified(true)
                .profileStatus(ProfileStatus.ACTIVE)
                .maritalStatus(userRequest.getMaritalStatus())
                .dateOfBirth(userRequest.getDateOfBirth())
                .admissionDate(userRequest.getAdmissionDate())
                .phoneNumber(userRequest.getPhoneNumber())
                .build();

        // Save Profile and update SessionClass
        Profile savedUserProfile = profileRepository.save(userProfile);
        if (sessionClass != null) {
            sessionClass.addProfile(savedUserProfile);
            sessionClass.setNumberOfProfiles(sessionClass.getProfiles().size());
            sessionClassRepository.save(sessionClass);
        }

        // Update school population
        school.incrementActualNumberOfStudents();
        schoolRepository.save(school);

        // Handle documents and referrals
        if (userRequest.getDocuments() != null && !userRequest.getDocuments().isEmpty()) {
            handleDocumentUploads(userRequest.getDocuments(), school, savedUserProfile);
        }
        if (userRequest.getReferralCode() != null && !userRequest.getReferralCode().isEmpty()) {
            processReferral(userRequest.getReferralCode(), savedUserProfile);
        }

        // Create wallet
        createWallet(savedUserProfile);

        // Send registration email
        sendRegistrationEmail(generatedPassword, savedUser, savedUserProfile.getUniqueRegistrationNumber());

        return buildUserResponse(savedUser, savedUserProfile);
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


    @Transactional
    public UserResponse selfRegisterStudent(UserRequestDto userRequest) {
        // Validate school exists
        School school = schoolRepository.findById(userRequest.getSchoolId())
                .orElseThrow(() -> new BadRequestException("Invalid school ID"));

        SessionClass sessionClass = sessionClassRepository.findBySessionIdAndClassBlockId(userRequest.getAcademicSessionId(), userRequest.getClassAssignedId())
                .orElseThrow(() -> new BadRequestException("Invalid class ID"));

        // Check if email already exists
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new BadRequestException("Email already registered");
        }
        BigDecimal applicationFee = null;
        boolean applicationFeeApplied = false;

        if (school.getIsApplicationFee()) {
            applicationFee = feeStructureService.getApplicationFee(
                    school.getId(),
                    sessionClass.getClassBlock().getClassLevel().getId(),
                    sessionClass.getClassBlock().getId()
            );
            applicationFeeApplied = true;
        }


        String generatedPassword = passwordGenerator.generateRandomPassword();
        String encodedPassword = passwordEncoder.encode(generatedPassword);

        String referralCode = generateReferralCode(
                userRequest.getFirstName(),
                userRequest.getLastName()
        );
       String referralLink = generateReferralLink(referralCode);

        // Create user with PENDING_REVIEW status
        User newUser = User.builder()
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .middleName(userRequest.getMiddleName())
                .school(school)
                .email(userRequest.getEmail())
                .roles(Collections.singleton(Roles.STUDENT))
                .profileStatus(ProfileStatus.PENDING_REVIEW)
                .isVerified(false)
                .password(encodedPassword)
                .build();
        User savedUser = userRepository.save(newUser);

        Set<EmergencyContact> emergencyContact = buildEmergencyContacts(userRequest.getEmergencyContacts());

        Set<Address> addresses = buildAddressesFromDto(userRequest.getAddresses());

        // Create profile
        Profile userProfile = Profile.builder()
                .user(savedUser)
                .profileStatus(ProfileStatus.PENDING_REVIEW)
                .gender(userRequest.getGender())
                .religion(userRequest.getReligion())
                .addresses(addresses)
                .emergencyContacts(emergencyContact)
                .referralCode(referralCode)
                .referralLink(referralLink)
                .studentGuardianOccupation(userRequest.getStudentGuardianOccupation())
                .studentGuardianOccupation(userRequest.getStudentGuardianOccupation())
                .studentGuardianName(userRequest.getStudentGuardianName())
                .studentGuardianPhoneNumber(userRequest.getStudentGuardianPhoneNumber())
                .uniqueRegistrationNumber(AccountUtils.generateStudentId(school.getSchoolCode()))
                .user(savedUser)
                .isVerified(true)
                .maritalStatus(userRequest.getMaritalStatus())
                .dateOfBirth(userRequest.getDateOfBirth())
                .admissionDate(userRequest.getAdmissionDate())
                .sessionClass(sessionClass)
//                .profilePicture(imageUrl)
                .phoneNumber(userRequest.getPhoneNumber())
                // ... other profile fields
                .build();
        Profile savedProfile = profileRepository.save(userProfile);

        //create wallet
        createWallet(savedProfile);


        // Handle document uploads if provided
        if (userRequest.getDocuments() != null) {
            handleDocumentUploads(userRequest.getDocuments(), school, savedProfile);
        }
         if(userRequest.getReferralCode() != null) {
          processReferral(userRequest.getReferralCode(), savedProfile);
                    }

        // Create admission application
        AdmissionApplication application = AdmissionApplication.builder()
                .profile(savedProfile)
                .school(school)
                .name(savedUser.getFirstName() + "  " + savedUser.getLastName())
                .status(ApplicationStatus.PENDING_REVIEW)
                .session(academicSessionRepository.findCurrentSession(school.getId()).get())
                .appliedClass(sessionClass.getClassBlock())
                .applicationFeeApplied(applicationFeeApplied)
                .applicationDate(LocalDateTime.now())
                .applicationFee(applicationFee)
                .build();
        admissionApplicationRepository.save(application);

        // Send confirmation email
        emailService.sendApplicationConfirmation(savedUser, generatedPassword, userProfile.getUniqueRegistrationNumber(),
                application.getApplicationNumber(), school);

        return buildUserResponse(savedUser, userProfile);
    }

    @Override
    @Transactional
    public UserResponse createAdmin(UserRequestDto userRequest) throws MessagingException {

        validateUserRequest(userRequest);

//                Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
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
                .roles(Collections.singleton(Roles.ADMIN))
                .build();
        User savedUser = userRepository.save(newUser);

        Set<Address> addresses = buildAddressesFromDto(userRequest.getAddresses());


        Profile userProfile =  Profile.builder()
                .gender(userRequest.getGender())
                .dateOfBirth(userRequest.getDateOfBirth())
                .religion(userRequest.getReligion())
                .isVerified(true)
                .profileStatus(ProfileStatus.ACTIVE)
                .schoolGraduatedFrom(userRequest.getSchoolGraduatedFrom())
                .phoneNumber(userRequest.getPhoneNumber())
                .addresses(addresses)
                .maritalStatus(userRequest.getMaritalStatus())
                .courseOfStudy(userRequest.getCourseOfStudy())
                .contractType(userRequest.getContractType())
                .academicQualification(userRequest.getAcademicQualification())
                .admissionDate(userRequest.getAdmissionDate())

                .uniqueRegistrationNumber(AccountUtils.generateAdminId())
                .dateOfBirth(userRequest.getDateOfBirth())
                .user(savedUser)
                //                .profilePicture(imageUrl)
                .phoneNumber(userRequest.getPhoneNumber())
                .build();
       profileRepository.save(userProfile);


        //create wallet
//        createWallet(saveUserProfile);
        sendAdminCreationEmail(savedUser, generatedPassword, userProfile.getUniqueRegistrationNumber());

        return buildUserResponse(savedUser, userProfile);
    }


    @Override
    @Transactional
    public UserResponse createStaff(UserRequestDto userRequest) throws MessagingException {

        String email = SecurityConfig.getAuthenticatedUserEmail();

        User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));

        StaffLevel staffLevel = staffLevelRepository.findByIdAndSchoolId(userRequest.getStaffLevelId(), admin.getSchool().getId())
                .orElseThrow(() -> new CustomNotFoundException("Staff level not found"));

         School school = admin.getSchool();


        validateUserRequest(userRequest);

        String generatedPassword = passwordGenerator.generateRandomPassword();
        String encodedPassword = passwordEncoder.encode(generatedPassword);

        String referralCode = generateReferralCode(
                userRequest.getFirstName(),
                userRequest.getLastName()
        );
        String referralLink = generateReferralLink(referralCode);

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
                .roles(Collections.singleton(userRequest.getRole()))
                .build();

        User savedUser = userRepository.save(newUser);
        Set<EmergencyContact> emergencyContact = buildEmergencyContacts(userRequest.getEmergencyContacts());
        Set<Address> addresses = buildAddressesFromDto(userRequest.getAddresses());
        Profile userProfile = buildStaffProfile(userRequest, savedUser, staffLevel, referralCode, referralLink, addresses, emergencyContact);
        Profile savedProfile = profileRepository.save(userProfile);
        createWallet(savedProfile);
        sendStaffCreationEmail(savedUser, generatedPassword, userProfile.getUniqueRegistrationNumber());


        //update the school population
        school.incrementActualNumberOfStaff();
        schoolRepository.save(school);

        // Handle document uploads if provided
        if (userRequest.getDocuments() != null) {
            handleDocumentUploads(userRequest.getDocuments(), school, savedProfile);
        }
        if (userRequest.getReferralCode() != null) {
            processReferral(userRequest.getReferralCode(), savedProfile);
        }

        // Return the response
        return buildUserResponse(savedUser, userProfile);
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
//            if (school != null && !school.isSubscriptionValid()) {
//                throw new SubscriptionExpiredException("Your subscription has expired. Please renew your subscription.");
//            }

            SecurityContextHolder.getContext().setAuthentication(authenticate);

            SchoolAuthDto schoolAuthDto = SchoolAuthDto.builder()
                    .id(school.getId())
                    .schoolName(school.getSchoolName())
                    .schoolAddress(school.getSchoolAddress())
                    .subscriptionType(school.getSubscriptionType() != null ? school.getSubscriptionType().toString() : null)
                    .phoneNumber(school.getPhoneNumber())
                    .subscriptionKey(school.getSubscriptionKey())
                    .subscriptionExpiryDate(school.getSubscriptionExpiryDate() != null ?
                            school.getSubscriptionExpiryDate().toLocalDate() : null)
                    .selectedServices(school.getSelectedServices() != null ?
                            school.getSelectedServices().stream()
                                    .map(ServiceOffered::getName)
                                    .collect(Collectors.toList()) : null)
                    .build();
          String  token = "Bearer " + jwtUtil.generateToken(user.getUser().getEmail(), schoolAuthDto);



            // Create a UserDto object containing user details
            UserDto userDto = UserDto.builder()
                    .firstName(userDetails.get().getFirstName())
                    .lastName(userDetails.get().getLastName())
                    .email(userDetails.get().getEmail())
                    .phoneNumber(user.getPhoneNumber())
                    .classAssigned(user.getSessionClass().getClassBlock().getName())
                    .studentGuardianName(user.getStudentGuardianName())
                    .studentGuardianPhoneNumber(user.getStudentGuardianPhoneNumber())
                    .uniqueRegistrationNumber(user.getUniqueRegistrationNumber())
                    .gender(user.getGender())
                    .isVerified(user.getIsVerified())
                    .address(user.getAddresses().isEmpty() ? null :
                            user.getAddresses().iterator().next().getStreet())
                    .academicQualification(user.getAcademicQualification())
                    .build();

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

            assert school != null;
            SchoolAuthDto schoolAuthDto = SchoolAuthDto.builder()
                    .id(school.getId())
                    .schoolName(school.getSchoolName())
                    .schoolAddress(school.getSchoolAddress())
                    .subscriptionType(school.getSubscriptionType() != null ? school.getSubscriptionType().toString() : null)
                    .phoneNumber(school.getPhoneNumber())
                    .subscriptionKey(school.getSubscriptionKey())
                    .subscriptionExpiryDate(school.getSubscriptionExpiryDate() != null ?
                            school.getSubscriptionExpiryDate().toLocalDate() : null)
                    .selectedServices(school.getSelectedServices() != null ?
                            school.getSelectedServices().stream()
                                    .map(ServiceOffered::getName)
                                    .collect(Collectors.toList()) : null)
                    .build();
        String    token = "Bearer " + jwtUtil.generateToken(user.getUser().getEmail(), schoolAuthDto);

            // Create a UserDto object containing user details
            UserDto userDto = UserDto.builder()
                    .firstName(userDetails.get().getFirstName())
                    .lastName(userDetails.get().getLastName())
                    .email(userDetails.get().getEmail())
                    .phoneNumber(user.getPhoneNumber())
                    .uniqueRegistrationNumber(user.getUniqueRegistrationNumber())
                    .gender(user.getGender())
                    .isVerified(user.getIsVerified())
                    .address(user.getAddresses().isEmpty() ? null :
                            user.getAddresses().iterator().next().getStreet())
                    .academicQualification(user.getAcademicQualification())
                    .build();
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
                // Generate token with SchoolAuthDto
                SchoolAuthDto schoolAuthDto = SchoolAuthDto.builder()
                        .id(school.getId())
                        .schoolName(school.getSchoolName())
                        .schoolAddress(school.getSchoolAddress())
                        .subscriptionType(school.getSubscriptionType() != null ? school.getSubscriptionType().toString() : null)
                        .phoneNumber(school.getPhoneNumber())
                        .subscriptionKey(school.getSubscriptionKey())
                        .subscriptionExpiryDate(school.getSubscriptionExpiryDate() != null ?
                                school.getSubscriptionExpiryDate().toLocalDate() : null)
                        .selectedServices(school.getSelectedServices() != null ?
                                school.getSelectedServices().stream()
                                        .map(ServiceOffered::getName)
                                        .collect(Collectors.toList()) : null)
                        .build();
                token = "Bearer " + jwtUtil.generateToken(user.getUser().getEmail(), schoolAuthDto);
            }

            SecurityContextHolder.getContext().setAuthentication(authenticate);
            // Create a UserDto object containing user details
            UserDto userDto = UserDto.builder()
                    .firstName(userDetails.get().getFirstName())
                    .lastName(userDetails.get().getLastName())
                    .email(userDetails.get().getEmail())
                    .phoneNumber(user.getPhoneNumber())
                    .uniqueRegistrationNumber(user.getUniqueRegistrationNumber())
                    .gender(user.getGender())
                    .isVerified(user.getIsVerified())
                    .address(user.getAddresses().isEmpty() ? null :
                            user.getAddresses().iterator().next().getStreet())
                    .academicQualification(user.getAcademicQualification())
                    .build();
            return new LoginResponse(token, userDto);
        } catch (BadCredentialsException e) {
            throw new AuthenticationFailedException("Wrong email or password");
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing JWT token", e);
        }
    }



    @Override
    public UserResponse editUserDetails(EditUserRequest editUserDto) {
        String email = SecurityConfig.getAuthenticatedUserEmail();   //why using this method and not autowired


        User user = userRepository.findByEmail(email).orElseThrow(()-> new UsernameNotFoundException("User not found"));

        if (user == null){
            throw  new CustomNotFoundException( "User does not exist");
        }
        user.setFirstName(editUserDto.getFirstName());
        user.setLastName(editUserDto.getLastName());
        User updatedUser = userRepository.save(user);

        Set<Address> addresses = new HashSet<>();
        if(editUserDto.getAddresses() != null){
            addresses = buildAddressesFromDto(editUserDto.getAddresses());

        }

        Profile userProfile =  Profile.builder()
                .gender(editUserDto.getGender())
                .dateOfBirth(editUserDto.getDateOfBirth())
                .religion(editUserDto.getReligion())
                .admissionDate(editUserDto.getAdmissionDate())
                .studentGuardianOccupation(editUserDto.getStudentGuardianOccupation())
                .studentGuardianOccupation(editUserDto.getStudentGuardianOccupation())
                .studentGuardianName(editUserDto.getStudentGuardianName())
                .studentGuardianPhoneNumber(editUserDto.getStudentGuardianPhoneNumber())
                .uniqueRegistrationNumber(AccountUtils.generateStudentId(user.getSchool().getSchoolCode()))
                .addresses(addresses)
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
            User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
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
            Long classLevelId,
            Long classBlockId,
            Long sessionId,
            String uniqueRegistrationNumber,
            String firstName,
            String lastName,
            int page,
            int size,
            String sortBy) {

        // Create Pageable object for pagination
        Pageable paging = PageRequest.of(page, size, Sort.by(sortBy).ascending());

        // Resolve optional filters
        SessionClass sessionClass = (sessionId != null && classBlockId != null) ?
                sessionClassRepository.findBySessionIdAndClassBlockId(sessionId, classBlockId)
                        .orElseThrow(() -> new CustomNotFoundException("SessionClass not found for Session ID: " + sessionId + " and ClassBlock ID: " + classBlockId)) :
                null;

        ClassLevel classLevel = classLevelId != null ?
                classLevelRepository.findById(classLevelId)
                        .orElseThrow(() -> new CustomNotFoundException("ClassLevel not found with ID: " + classLevelId)) :
                null;

        AcademicSession academicSession = sessionId != null ?
                academicSessionRepository.findById(sessionId)
                        .orElseThrow(() -> new CustomNotFoundException("Academic session not found with ID: " + sessionId)) :
                null;

        // Fetch students with optional filters
        Page<Profile> students = profileRepository.findAllWithFilters(
                sessionClass,
                classLevel,
                academicSession,
                uniqueRegistrationNumber,
                firstName,
                lastName,
                paging);

        return students.map(element -> modelMapper.map(element, UserResponse.class));
    }

    private boolean isSubscriptionExpired(School school) {
        LocalDateTime expiryDate = school.getSubscriptionExpiryDate();
        return expiryDate != null && expiryDate.isBefore(LocalDateTime.now());
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

    @Override

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


    @Transactional(readOnly = true)
    public SchoolActiveUsersResponse getActiveUsersStatistics() {
        String email = SecurityConfig.getAuthenticatedUserEmail();


        User loggedUser = userRepository.findByEmail(email).orElseThrow(()-> new UsernameNotFoundException("User not found"));
        // Get all active users in the school
        List<User> activeUsers = userRepository.findBySchoolIdAndProfileStatus(loggedUser.getSchool().getId(), ProfileStatus.ACTIVE);

        if (activeUsers.isEmpty()) {
            return SchoolActiveUsersResponse.builder()
                    .numOfStudents(0)
                    .numOfStaff(0)
                    .percentOfBoys(BigDecimal.ZERO)
                    .percentOfGirls(BigDecimal.ZERO)
                    .percentOfMaleStaff(BigDecimal.ZERO)
                    .percentOfFemaleStaff(BigDecimal.ZERO)
                    .build();
        }

        // Count students and staff
        long numStudents = activeUsers.stream()
                .filter(user -> user.hasRole(Roles.STUDENT))
                .count();

        long numStaff = activeUsers.size() - numStudents;

        // Get profiles for gender statistics
        List<Profile> profiles = profileRepository.findByUserIn(activeUsers);

        // Calculate student gender percentages
        long maleStudents = profiles.stream()
                .filter(profile -> profile.getUser().hasRole(Roles.STUDENT))
                .filter(profile -> "male".equalsIgnoreCase(profile.getGender()))
                .count();

        long femaleStudents = numStudents - maleStudents;

        BigDecimal percentBoys = numStudents > 0 ?
                BigDecimal.valueOf(maleStudents * 100.0 / numStudents)
                        .setScale(2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        BigDecimal percentGirls = numStudents > 0 ?
                BigDecimal.valueOf(femaleStudents * 100.0 / numStudents)
                        .setScale(2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        // Calculate staff gender percentages
        long maleStaff = profiles.stream()
                .filter(profile -> profile.getUser().hasRole(Roles.STUDENT))
                .filter(profile -> "male".equalsIgnoreCase(profile.getGender()))
                .count();

        long femaleStaff = numStaff - maleStaff;

        BigDecimal percentMaleStaff = numStaff > 0 ?
                BigDecimal.valueOf(maleStaff * 100.0 / numStaff)
                        .setScale(2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        BigDecimal percentFemaleStaff = numStaff > 0 ?
                BigDecimal.valueOf(femaleStaff * 100.0 / numStaff)
                        .setScale(2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        return SchoolActiveUsersResponse.builder()
                .numOfStudents((int) numStudents)
                .numOfStaff((int) numStaff)
                .percentOfBoys(percentBoys)
                .percentOfGirls(percentGirls)
                .percentOfMaleStaff(percentMaleStaff)
                .percentOfFemaleStaff(percentFemaleStaff)
                .build();
    }


    private Profile buildStaffProfile(UserRequestDto userRequest, User savedUser, StaffLevel staffLevel, String referralCode, String referralLink,Set<Address> addresses, Set<EmergencyContact> emergencyContact
    ) {
        Profile.ProfileBuilder profileBuilder = Profile.builder()
                .gender(userRequest.getGender())
                .isVerified(true)
                .staffLevel(staffLevel)
                .referralCode(referralCode)
                .referralLink(referralLink)
                .profileStatus(ProfileStatus.ACTIVE)
                .dateOfBirth(userRequest.getDateOfBirth())
                .courseOfStudy(userRequest.getCourseOfStudy())
                .classOfDegree(userRequest.getClassOfDegree())
                .admissionDate(userRequest.getAdmissionDate())
                .addresses(addresses)
                .contractType(userRequest.getContractType())
                .maritalStatus(userRequest.getMaritalStatus())
                .schoolGraduatedFrom(userRequest.getSchoolGraduatedFrom())
                .academicQualification(userRequest.getAcademicQualification())
                .religion(userRequest.getReligion())
                .uniqueRegistrationNumber(AccountUtils.generateStaffId(savedUser.getSchool().getSchoolCode()))
                .emergencyContacts(emergencyContact)
                .phoneNumber(userRequest.getPhoneNumber())
                .user(savedUser);

        if (userRequest.getClassFormTeacherId() != null) {
            ClassBlock classBlock = classBlockRepository.findByIdAndSchoolId(userRequest.getClassFormTeacherId(), savedUser.getSchool().getId())
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
        userWallet.setWalletStatus(WalletStatus.ACTIVE);
        userWallet.setSchool(userProfile.getUser().getSchool());
        walletRepository.save(userWallet);
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

    private void sendAdminCreationEmail(User savedUser, String generatedPassword, String regNo) throws MessagingException {

        Map<String, Object> model = new HashMap<>();
        model.put("name", savedUser.getFirstName() + " " + savedUser.getLastName());
        model.put("username", regNo);
        model.put("password", generatedPassword);

        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(savedUser.getEmail())
                .subject("ACCOUNT CREATION")
                .templateName("email-template-admin")
                .model(model)
                .build();
        emailService.sendHtmlEmail(emailDetails);

    }

    private UserResponse buildUserResponse(User savedUser, Profile savedUserProfile) {
        return UserResponse.builder()
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .email(savedUser.getEmail())
                .phoneNumber(savedUserProfile.getPhoneNumber())
                .gender(savedUserProfile.getGender())
                .admissionDate(savedUserProfile.getAdmissionDate())
                .uniqueRegistrationNumber(savedUserProfile.getUniqueRegistrationNumber())
                .academicQualification(savedUserProfile.getAcademicQualification())
                .address(savedUserProfile.getAddresses().isEmpty() ? null
                        : savedUserProfile.getAddresses().iterator().next().getStreet()) // handle address properly
                .build();
    }



    private String generateReferralCode(String firstName, String lastName) {
        String base = (firstName.charAt(0) + lastName.substring(0, 1)).toUpperCase();
        String randomDigits = String.format("%04d", new Random().nextInt(10000));

        String code = base + randomDigits;

        // Ensure uniqueness
        while (profileRepository.existsByReferralCode(code)) {
            randomDigits = String.format("%04d", new Random().nextInt(10000));
            code = base + randomDigits;
        }

        return code;
    }

    private String generateReferralLink(String referralCode) {
        String baseUrl = "https://yourapp.com/signup?ref="; // Replace with your actual domain
        return baseUrl + referralCode;
    }


    private void handleDocumentUploads(
            List<UserRequestDto.DocumentDto> documentDtos,
            School school,
            Profile profile) {

        if (documentDtos == null || documentDtos.isEmpty()) {
            return;
        }

        List<Document> documents = new ArrayList<>();

        for (UserRequestDto.DocumentDto docRequest : documentDtos) {
            try {
                Document document = processSingleDocument(docRequest, school, profile);
                documents.add(document);
            } catch (Exception e) {
                throw new BadRequestException("Failed to upload document: " + docRequest.getDocumentType());
            }
        }

        documentRepository.saveAll(documents);
    }

    private Document processSingleDocument(
            UserRequestDto.DocumentDto docRequest,
            School school,
            Profile profile) throws Exception {

//         Upload file to Cloudinary and get URL
//         Map<?, ?> uploadResult = cloudinaryService.uploadFile(docRequest.getFile());
//         String documentUrl = (String) uploadResult.get("secure_url");
        String documentUrl = "documentUrl"; // Replace with actual upload logic

        return Document.builder()
                .title(docRequest.getDocumentType() + " for " + profile.getUser().getFirstName() +
                        " " + profile.getUser().getLastName())
                .school(school)
                .profile(profile)
                .documentType(docRequest.getDocumentType())
                .documentImageUrl(documentUrl)
                .documentNo(docRequest.getDocumentNo())
                .build();
    }


    private void processReferral(String referralCode, Profile referredProfile) {
        if (!StringUtils.hasText(referralCode)) {
            return;
        }

        Profile referringUser = profileRepository.findByReferralCode(referralCode)
                .orElseThrow(() -> new BadRequestException("Invalid referral code"));

        Referral referral = Referral.builder()
                .referringUser(referringUser)
                .referredUser(referredProfile)
                .status(ReferralStatus.PENDING)
                .school(referringUser.getUser().getSchool())
                .referralDate(LocalDateTime.now())
                .build();

        referralRepository.save(referral);
    }


    private Set<Address> buildAddressesFromDto(Set<UserRequestDto.AddressDto> addressDtos) {
        Set<Address> addresses = new HashSet<>();
        for (UserRequestDto.AddressDto addressDto : addressDtos) {
            Address address = Address.builder()
                    .street(addressDto.getStreet())
                    .city(addressDto.getCity())
                    .state(addressDto.getState())
                    .country(addressDto.getCountry())
                    .postalCode(addressDto.getPostalCode())
                    .isPrimary(addressDto.isPrimary())
                    .build();
            addresses.add(address);
        }
        return addresses;
    }

    private Set<EmergencyContact> buildEmergencyContacts(List<UserRequestDto.EmergencyContactDto> contactDtos) {
        if (contactDtos == null || contactDtos.isEmpty()) {
            return new HashSet<>(); // Return empty set if no contacts provided
        }

        Set<EmergencyContact> emergencyContacts = new HashSet<>();
        Set<String> phoneNumbers = new HashSet<>(); // Track phones to prevent duplicates

        for (UserRequestDto.EmergencyContactDto dto : contactDtos) {
            EmergencyContact contact = buildEmergencyContact(dto);
            if (contact != null) {
                // Check for duplicate phone numbers
                if (!phoneNumbers.add(contact.getPhone())) {
                    throw new BadRequestException("Duplicate emergency contact phone number: " + contact.getPhone());
                }
                emergencyContacts.add(contact);
            }
        }

        return emergencyContacts;
    }


    private EmergencyContact buildEmergencyContact(UserRequestDto.EmergencyContactDto emergencyContactDto) {
        if (emergencyContactDto == null) {
            return null; // Skip null DTOs, handled by caller
        }

        // Validate required fields
        if (emergencyContactDto.getName() == null || emergencyContactDto.getName().trim().isEmpty()) {
            throw new BadRequestException("Emergency contact name cannot be empty");
        }
        if (emergencyContactDto.getPhone() == null || emergencyContactDto.getPhone().trim().isEmpty()) {
            throw new BadRequestException("Emergency contact phone cannot be empty");
        }
        if (emergencyContactDto.getRelationship() == null || emergencyContactDto.getRelationship().trim().isEmpty()) {
            throw new BadRequestException("Emergency contact relationship cannot be empty");
        }

        // Validate phone format (optional, adjust regex as needed)
        String phone = emergencyContactDto.getPhone().trim();
        if (!phone.matches("^\\+?[1-9]\\d{1,14}$")) {
            throw new BadRequestException("Invalid emergency contact phone number format: " + phone);
        }

        return EmergencyContact.builder()
                .name(emergencyContactDto.getName().trim())
                .phone(phone)
                .relationship(emergencyContactDto.getRelationship().trim())
                .build();
    }


}


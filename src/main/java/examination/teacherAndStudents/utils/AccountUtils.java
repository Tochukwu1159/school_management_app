package examination.teacherAndStudents.utils;

import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.error_handler.*;
import examination.teacherAndStudents.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.Contract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Component
public class AccountUtils {
    public static final String PAGENO = "0";
    public static final String PAGESIZE = "10";

    public static final String PAYSTACK_TRANSACTION_INITIALIZER ="https://api.paystack.co/transaction/initialize";
    public static final String PAYSTACK_BULK_TRANSFER_URL = "https://api.paystack.co/transfer/bulk";
    private static WalletRepository walletRepository;

    // Inject repository via constructor
    public AccountUtils(WalletRepository repository) {
        walletRepository = repository;
    }

    @Contract(value = "_, null -> false", pure = true)
    public static boolean validatePassword(String password, String cpassword) {
        return password.equals(cpassword);
    }


    private static UserRepository userRepository;
    private static ProfileRepository profileRepository;
    private static LibraryMemberRepository libraryMemberRepository;
    private static SchoolRepository schoolRepository;

    // Constructor-based injection
    @Autowired
    public AccountUtils(UserRepository userRepository, ProfileRepository profileRepository, LibraryMemberRepository libraryMemberRepository, WalletRepository walletRepository) {
        AccountUtils.userRepository = userRepository;
        AccountUtils.profileRepository = profileRepository;
        AccountUtils.libraryMemberRepository = libraryMemberRepository;
        AccountUtils.walletRepository = walletRepository;
    }




    public static final String generateStudentId(String schoolCode) {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String yearString = String.valueOf(currentYear);

        SecureRandom random = new SecureRandom();
        String studentId;
        do {
            StringBuilder randomNumbers = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                int randomNumber = random.nextInt(10);
                randomNumbers.append(randomNumber);
            }
            studentId = yearString + schoolCode + randomNumbers;

        } while (profileRepository.existsByUniqueRegistrationNumber(studentId));
        return studentId;
    }

    public static final String generateAdminId(String schoolCode) {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String yearString = String.valueOf(currentYear);

        SecureRandom random = new SecureRandom();
        String adminId;

        do {
            StringBuilder randomNumbers = new StringBuilder();
            // Generate 4 random numbers
            for (int i = 0; i < 4; i++) {
                int randomNumber = random.nextInt(10);
                randomNumbers.append(randomNumber);
            }

            // Combine current year + schoolCode + 4 random numbers
            adminId = yearString + schoolCode + randomNumbers;
        } while (profileRepository.existsByUniqueRegistrationNumber(adminId));

        return adminId;
    }

    public static final String generateStaffId(String schoolCode) {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String yearString = String.valueOf(currentYear);

        SecureRandom random = new SecureRandom();
        String staffId;

        do {
            StringBuilder randomNumbers = new StringBuilder();
            // Generate 4 random numbers
            for (int i = 0; i < 4; i++) {
                int randomNumber = random.nextInt(10);
                randomNumbers.append(randomNumber);
            }

            // Combine current year + schoolCode + 4 random numbers
            staffId = yearString + schoolCode + randomNumbers;
        } while (profileRepository.existsByUniqueRegistrationNumber(staffId));

        return staffId;
    }

    public static final String generateLibraryId() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String yearString = String.valueOf(currentYear);

        SecureRandom random = new SecureRandom();
        String libraryId;

        do {
            StringBuilder randomNumbers = new StringBuilder();
            // Generate 3 random numbers
            for (int i = 0; i < 6; i++) {
                int randomNumber = random.nextInt(10);
                randomNumbers.append(randomNumber);
            }

            // Combine "ADMIN" + current year + 3 random numbers
            libraryId = "LIB" + yearString + randomNumbers;
        } while (libraryMemberRepository.existsByMemberId(libraryId));

        return libraryId;
    }


    public static final String generateSchoolCode() {
        SecureRandom random = new SecureRandom();
        String schoolCode;

        do {
            StringBuilder randomNumbers = new StringBuilder();
            // Generate 4 random digits
            for (int i = 0; i < 4; i++) {
                int randomNumber = random.nextInt(10);
                randomNumbers.append(randomNumber);
            }
            schoolCode = String.valueOf(randomNumbers);
        } while (schoolRepository.existsBySchoolCode(schoolCode));

        return schoolCode;
    }

    public static void validateProfileStatus(Profile profile) {
        // Check if profile is suspended
        if (ProfileStatus.SUSPENDED.equals(profile.getProfileStatus()) &&
                profile.getSuspensionEndDate() != null &&
                LocalDate.now().isAfter(profile.getSuspensionEndDate())) {
            // Reactivate if the suspension period is over
            profile.setProfileStatus(ProfileStatus.ACTIVE);
        }

        // General check for restricted statuses
        if (ProfileStatus.SUSPENDED.equals(profile.getProfileStatus()) ||
                ProfileStatus.FIRED.equals(profile.getProfileStatus()) ||
                ProfileStatus.GRADUATED.equals(profile.getProfileStatus()) ||
                ProfileStatus.ALUMNI.equals(profile.getProfileStatus()) ||
                ProfileStatus.WITHDRAWN.equals(profile.getProfileStatus()) ||
                ProfileStatus.TRANSFERRING.equals(profile.getProfileStatus())) {
            throw new UnauthorizedException("Profile cannot access because you are " + profile.getProfileStatus().name().toLowerCase());
        }
    }


    public static final String localDateTimeConverter(LocalDateTime localDateTime){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM , yyyy hh:mm a", Locale.US);
        return formatter.format(localDateTime);
    }

    public static final boolean isValidEmail(String email) {
        String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";

        // Compile the ReGex
        Pattern p = Pattern.compile(regex);
        if (email == null) {
            throw new BadRequestException("Error: Email cannot be null");
        }
        Matcher m = p.matcher(email);
        return m.matches();
    }
    public static final boolean existsByMail(String email) {
        return userRepository.existsByEmail(email);


    }


    public static final void validateEmailAndPassword(String email, String password, String confirmPassword) {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistException("User with email already exists");
        }

        if (!AccountUtils.validatePassword(password, confirmPassword)) {
            throw new UserPasswordMismatchException("Password does not match");
        }

        if (AccountUtils.existsByMail(email)) {
            throw new BadRequestException("Error: Email is already taken!");
        }

        if (!AccountUtils.isValidEmail(email)) {
            throw new BadRequestException("Error: Email must be valid");
        }

        if (password.length() < 8 || confirmPassword.length() < 8) {
            throw new BadRequestException("Password is too short, should be a minimum of 8 characters long");
        }
    }


    public static String getAuthenticatedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationFailedException("User is not authenticated");
        }
        return authentication.getName();
    }

    public static String generateSubscriptionKey() {
        return RandomStringUtils.randomAlphanumeric(16);
    }




}

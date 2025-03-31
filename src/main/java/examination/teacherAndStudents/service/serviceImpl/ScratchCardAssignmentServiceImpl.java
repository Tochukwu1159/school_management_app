package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.ScratchCardDTO;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.BadRequestException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.ScratchCardAssignmentService;
import examination.teacherAndStudents.utils.EncryptionUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ScratchCardAssignmentServiceImpl implements ScratchCardAssignmentService {
    private final ProfileRepository profileRepository;
    private final SchoolRepository schoolRepository;
    private final ScratchCardRepository scratchCardRepository;
    private final WalletRepository walletRepository;
    private final AcademicSessionRepository academicSessionRepository;
    private final StudentTermRepository studentTermRepository;


    public ScratchCardAssignmentServiceImpl(ProfileRepository profileRepository, SchoolRepository schoolRepository, ScratchCardRepository scratchCardRepository, UserRepository userRepository, WalletRepository walletRepository, WalletRepository walletRepository1, AcademicSessionRepository academicSessionRepository, StudentTermRepository studentTermRepository, EncryptionUtil encryptionUtil) {
        this.profileRepository = profileRepository;
        this.schoolRepository = schoolRepository;
        this.scratchCardRepository = scratchCardRepository;
        this.walletRepository = walletRepository1;
        this.academicSessionRepository = academicSessionRepository;
        this.studentTermRepository = studentTermRepository;

    }

    @Override
    public ScratchCardDTO buyScratch(Long sessionId, Long termId) throws Exception {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        Profile profile = profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("Please login"));

        School school = schoolRepository.findById(profile.getUser().getSchool().getId())
                .orElseThrow(() -> new BadRequestException("School not found"));

        AcademicSession session = academicSessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomNotFoundException("Academic session not found"));

        StudentTerm term = studentTermRepository.findById(termId)
                .orElseThrow(() -> new CustomNotFoundException("Term not found"));


        ScratchCard card = scratchCardRepository
                .findFirstAvailableBySchoolSessionAndTerm(
                        school.getId(),
                        session.getId(),
                        term.getId())
                .orElseThrow(() -> new BadRequestException(
                        "No available cards for %s %s".formatted(session.getName(), term.getName())));

        Wallet studentWallet = walletRepository.findWalletByUserProfile(profile).orElseThrow(() -> new BadRequestException("Student wallet not found"));

        studentWallet.debit(card.getPrice());

        card.setStudent(profile);
        card.setAssignedAt(LocalDateTime.now());
        scratchCardRepository.save(card);
        return buildScratchCardDTO(card);

    }

    @Override
    public ScratchCardDTO getStudentScratchCard(Long sessionId, Long termId) {
        // Validate school exists
        String email = SecurityConfig.getAuthenticatedUserEmail();
        Profile studentProfile = profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("Please login"));
        Long schoolId = studentProfile.getUser().getSchool().getId();

        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new CustomNotFoundException("School not found"));

        // Validate session exists
        AcademicSession session = academicSessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomNotFoundException("Academic session not found"));

        // Validate term exists
        StudentTerm term = studentTermRepository.findById(termId)
                .orElseThrow(() -> new CustomNotFoundException("Term not found"));

        // Find the assigned scratch card
        ScratchCard card = scratchCardRepository
                .findBySchoolIdAndAcademicSessionIdAndStudentTermIdAndStudentId(
                        schoolId,
                        sessionId,
                        termId,
                        studentProfile.getId())
                .orElseThrow(() -> new CustomNotFoundException(
                        "No scratch card found for student %s in %s %s".formatted(
                                studentProfile.getUser().getFirstName() +  " " + studentProfile.getUser().getLastName(),
                                session.getName(),
                                term.getName())));

        try {
            return buildScratchCardDTO(card);
        } catch (Exception e) {
            throw new BadRequestException("Error processing scratch card data");
        }
    }

        private ScratchCardDTO buildScratchCardDTO(ScratchCard card) throws Exception {
            return ScratchCardDTO.builder()
                    .cardNumber(card.getCardNumber())
                    .schoolId(card.getSchool().getId())
                    .price(card.getPrice())
                    .pin(EncryptionUtil.decrypt(card.getPin()))
                    .maxUsageCount(card.getMaxUsageCount())
                    .sessionId(card.getAcademicSession().getId())
                    .sessionName(card.getAcademicSession().getName())
                    .termId(card.getStudentTerm().getId())
                    .termName(card.getStudentTerm().getName())
                    .createdAt(card.getCreatedAt())
                    .expiryDate(card.getExpiryDate())
                    .build();

    }

}

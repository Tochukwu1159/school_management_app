package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.ScratchCardDTO;
import examination.teacherAndStudents.dto.ScratchCardPurchaseRequest;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.BadRequestException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.ScratchCardAssignmentService;
import examination.teacherAndStudents.utils.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScratchCardAssignmentServiceImpl implements ScratchCardAssignmentService {
    private final ProfileRepository profileRepository;
    private final SchoolRepository schoolRepository;
    private final ScratchCardRepository scratchCardRepository;
    private final WalletRepository walletRepository;
    private final AcademicSessionRepository academicSessionRepository;
    private final StudentTermRepository studentTermRepository;

    @Override
    public ScratchCardDTO buyScratch(ScratchCardPurchaseRequest request) throws Exception {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        Profile profile = profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("Please login"));

        // Validate school
        School school = schoolRepository.findById(profile.getUser().getSchool().getId())
                .orElseThrow(() -> new BadRequestException("School not found"));

        // Validate academic session
        AcademicSession session = academicSessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new CustomNotFoundException("Academic session not found"));

        // Validate term
        StudentTerm term = studentTermRepository.findById(request.getTermId())
                .orElseThrow(() -> new CustomNotFoundException("Term not found"));

        // Validate subclass (ClassBlock)
        ClassBlock classBlock = profile.getClassBlock();

        // Ensure the class block belongs to the student's school
        if (!classBlock.getClassLevel().getSchool().getId().equals(school.getId())) {
            throw new BadRequestException("Selected subclass does not belong to your school");
        }

        // Ensure the student is enrolled in the class block
        if (!classBlock.getStudentList().contains(profile)) {
            throw new BadRequestException("You are not enrolled in the selected subclass");
        }

        // Validate student wallet
        Wallet studentWallet = walletRepository.findWalletByUserProfile(profile)
                .orElseThrow(() -> new BadRequestException("Student wallet not found"));

        // Check if the student has enough balance
        if (studentWallet.getBalance().compareTo(school.getScratchCardPrice()) < 0) {
            throw new BadRequestException("Insufficient balance to purchase scratch card");
        }

        // Generate a new scratch card
        String cardNumber = "SC" + UUID.randomUUID().toString().substring(0, 23).toUpperCase();
        String pin = String.format("%04d", new Random().nextInt(1000000));

        ScratchCard card = ScratchCard.builder()
                .cardNumber(cardNumber)
                .pin(EncryptionUtil.encrypt(pin))
                .price(school.getScratchCardPrice())
                .maxUsageCount(school.getScratchCardMaxUsageCount())
                .currentUsageCount(0)
                .isActive(true)
                .school(school)
                .academicSession(session)
                .studentTerm(term)
                .student(profile)
                .assignedAt(LocalDateTime.now())
                .build();

        // Debit the student's wallet
        studentWallet.debit(card.getPrice());
        walletRepository.save(studentWallet);

        // Save the scratch card
        scratchCardRepository.save(card);

        return buildScratchCardDTO(card);
    }

    @Override
    public ScratchCardDTO getStudentScratchCard(Long sessionId, Long termId) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        Profile studentProfile = profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("Please login"));
        Long schoolId = studentProfile.getUser().getSchool().getId();

        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new CustomNotFoundException("School not found"));

        AcademicSession session = academicSessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomNotFoundException("Academic session not found"));

        StudentTerm term = studentTermRepository.findById(termId)
                .orElseThrow(() -> new CustomNotFoundException("Term not found"));

        ScratchCard card = scratchCardRepository
                .findBySchoolIdAndAcademicSessionIdAndStudentTermIdAndStudentId(
                        schoolId,
                        sessionId,
                        termId,
                        studentProfile.getId())
                .orElseThrow(() -> new CustomNotFoundException(
                        "No scratch card found for student %s in %s %s".formatted(
                                studentProfile.getUser().getFirstName() + " " + studentProfile.getUser().getLastName(),
                                session.getSessionName().getName(),
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
                .sessionName(card.getAcademicSession().getSessionName().getName())
                .termId(card.getStudentTerm().getId())
                .termName(card.getStudentTerm().getName())
                .createdAt(card.getCreatedAt())
                .expiryDate(card.getExpiryDate())
                .build();
    }
}
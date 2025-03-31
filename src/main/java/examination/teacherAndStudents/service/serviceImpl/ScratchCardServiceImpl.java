package examination.teacherAndStudents.service.serviceImpl;


import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.GenerateScratchCardsRequest;
import examination.teacherAndStudents.dto.ScratchCardDTO;
import examination.teacherAndStudents.dto.ScratchCardValidationRequest;
import examination.teacherAndStudents.dto.ScratchCardValidationResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.AuthenticationFailedException;
import examination.teacherAndStudents.error_handler.BadRequestException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.ScratchCardException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.ScratchCardService;
import examination.teacherAndStudents.utils.EncryptionUtil;
import examination.teacherAndStudents.utils.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;


@Service
@RequiredArgsConstructor
public class ScratchCardServiceImpl implements ScratchCardService {
    private final ScratchCardRepository scratchCardRepository;
    private final ScratchCardUsageRepository usageRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final AcademicSessionRepository academicSessionRepository;
    private final StudentTermRepository studentTermRepository;

    // Generate new scratch cards (admin function)
    public List<ScratchCardDTO>  generateScratchCards(GenerateScratchCardsRequest generateScratchCardsRequest) throws Exception {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN)
                .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));

        AcademicSession session = academicSessionRepository.findById(generateScratchCardsRequest.getSessionId())
                .orElseThrow(() -> new CustomNotFoundException("Academic session not found"));

        StudentTerm studentTerm = studentTermRepository.findById(generateScratchCardsRequest.getTermId())
                .orElseThrow(() -> new CustomNotFoundException("Term not found"));

        List<ScratchCard> cards = new ArrayList<>();
        List<ScratchCardDTO> response = new ArrayList<>();
        for (int i = 0; i < generateScratchCardsRequest.getQuantity(); i++) {
            String cardNumber = "SC" + UUID.randomUUID().toString().substring(0, 23).toUpperCase();
            String pin = generateRandomPin();

            ScratchCard card = ScratchCard.builder()
                    .cardNumber(cardNumber)
                    .pin(EncryptionUtil.encrypt(pin)) // Encrypt PIN
                    .currentUsageCount(0)
                    .maxUsageCount(generateScratchCardsRequest.getMaxUsageCount())
                    .isActive(true)
                    .school(admin.getSchool())
                    .expiryDate(generateScratchCardsRequest.getExpiration())
                    .price(generateScratchCardsRequest.getPrice())
                    .academicSession(session) // New
                    .studentTerm(studentTerm)
                    .build();

            cards.add(card);
            response.add(ScratchCardDTO.builder()
                                    .cardNumber(cardNumber)
                                            .price(generateScratchCardsRequest.getPrice())
                                                    .pin(pin)
                            .createdAt(card.getCreatedAt())
                            .sessionId(card.getAcademicSession().getId())
                            .sessionName(card.getAcademicSession().getName())
                            .termId(card.getAcademicSession().getId())
                            .termName(card.getStudentTerm().getName())
                                                            .maxUsageCount(card.getMaxUsageCount())
                                                                    .expiryDate(card.getExpiryDate()).build());

        }
         scratchCardRepository.saveAll(cards);
        return response;
    }

    private String generateRandomPin() {
        return String.format("%04d", new Random().nextInt(1000000));
    }

    public Page<ScratchCardDTO> getGeneratedScratchCards(int page, int size, Long sessionId, Long termId) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN)
                .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<ScratchCard> cards = scratchCardRepository.findBySchoolAndOptionalSessionAndTerm(
                admin.getSchool().getId(),
                sessionId,
                termId,
                pageable);

        return cards.map(this::mapToResponse);
    }

    private ScratchCardDTO mapToResponse(ScratchCard card) {
        return ScratchCardDTO.builder()
                .cardNumber(card.getCardNumber())
                .price(card.getPrice())
                .maxUsageCount(card.getMaxUsageCount())
                .currentUsageCount(card.getCurrentUsageCount())
                .isActive(card.getIsActive())
                .expiryDate(card.getExpiryDate())
                .createdAt(card.getCreatedAt())
                .sessionId(card.getAcademicSession().getId())
                .sessionName(card.getAcademicSession().getName())
                .termId(card.getStudentTerm().getId())
                .termName(card.getStudentTerm().getName())
                .build();
    }

    // Validate and use scratch card
    public ScratchCardValidationResponse validateScratchCard( ScratchCardValidationRequest request) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        Profile profile = profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("Please login"));

        ScratchCard card = scratchCardRepository.findByCardNumber(request.getCardNumber())
                .orElseThrow(() -> new CustomNotFoundException("Invalid scratch card"));

        if (profile.getUser().getSchool() == null || card.getSchool() == null ||
                !profile.getUser().getSchool().getId().equals(card.getSchool().getId())) {
            throw new BadRequestException("The card does not belong to your school");
        }

        if (card.getStudent() == null || !card.getStudent().getId().equals(profile.getId())) {
            throw new AuthenticationFailedException("This card is not assigned to you. Please purchase first.");
        }
        // Check if card is valid
        if (!card.getIsActive()) {
            throw new BadRequestException("Scratch card has been deactivated or you have exceeded maximum usage");
        }

        if (card.getCurrentUsageCount() >= card.getMaxUsageCount()) {
            throw new BadRequestException("Scratch card usage limit exceeded");
        }

        List<ScratchCardUsage> existingUsages = usageRepository.findByScratchCard(card);

        if (!existingUsages.isEmpty()) {
            // Get the first student who used this card
            Profile firstUser = existingUsages.get(0).getStudent();

            if (!firstUser.getId().equals(profile.getId())) {
                throw new BadRequestException("This scratch card has already been activated by another student");
            }
        }

        if (card.getCurrentUsageCount() >= card.getMaxUsageCount()) {
            card.setIsActive(false);
            scratchCardRepository.save(card);
            throw new ScratchCardException("Scratch card usage limit (5) reached");
        }

        if (LocalDateTime.now().isAfter(card.getExpiryDate())) {
            throw new BadRequestException("Scratch card has expired");
        }

        if (!passwordEncoder.matches(request.getPin(), card.getPin())) {
            throw new BadRequestException("Invalid PIN");
        }


        // Record the usage
        ScratchCardUsage usage = ScratchCardUsage.builder()
                .scratchCard(card)
                .student(profile)
                .build();
        usageRepository.save(usage);

        // Update usage count
        card.setCurrentUsageCount(card.getCurrentUsageCount() + 1);
        if (card.getCurrentUsageCount() >= card.getMaxUsageCount()) {
            card.setIsActive(false);
        }
        scratchCardRepository.save(card);

        return ScratchCardValidationResponse.builder()
                .remainingUses(card.getMaxUsageCount() - card.getCurrentUsageCount())
                .cardNumber(card.getCardNumber())
                .validUntil(card.getExpiryDate())
                .build();
    }
}
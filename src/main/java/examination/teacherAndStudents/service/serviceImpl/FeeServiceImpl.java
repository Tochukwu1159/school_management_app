package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.FeeDTO;
import examination.teacherAndStudents.dto.StudentFeeResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.BadRequestException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.ResourceNotFoundException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.FeeService;
import examination.teacherAndStudents.utils.FeeStatus;
import examination.teacherAndStudents.dto.FeeResponseDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Implementation of FeeService for managing fees.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class FeeServiceImpl implements FeeService {

    private static final Logger logger = LoggerFactory.getLogger(FeeServiceImpl.class);

    private final FeeRepository feeRepository;
    private final FeeCategoryRepository feeCategoryRepository;
    private final AcademicSessionRepository academicSessionRepository;
    private final ClassLevelRepository classLevelRepository;
    private final ClassBlockRepository classBlockRepository;
    private final StudentTermRepository studentTermRepository;
    private final ProfileRepository profileRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public FeeResponseDto createFee(FeeDTO feeDTO) {
        validateFeeDTO(feeDTO);
        String email = SecurityConfig.getAuthenticatedUserEmail();
        Profile admin = profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));
        Fee fee = new Fee();
        fee.setCategory(feeCategoryRepository.findById(feeDTO.getCategoryId())
                .orElseThrow(() -> new CustomNotFoundException("Fee category not found with ID: " + feeDTO.getCategoryId())));
        fee.setDescription(feeDTO.getDescription());
        fee.setAmount(feeDTO.getAmount());
        fee.setCompulsory(feeDTO.isCompulsory());

        fee.setSchool(admin.getUser().getSchool());

        AcademicSession academicSession = academicSessionRepository.findByIdAndSchoolId(feeDTO.getSessionId(), admin.getUser().getSchool().getId())
                .orElseThrow(() -> new CustomNotFoundException("Academic Session not found with ID: " + feeDTO.getSessionId()));
        fee.setSession(academicSession);

        ClassLevel classLevel = null;
        if (feeDTO.getClassLevelId() != null) {
            classLevel = classLevelRepository.findByIdAndSchoolId(feeDTO.getClassLevelId(), admin.getUser().getSchool().getId())
                    .orElseThrow(() -> new CustomNotFoundException("Class Level not found with ID: " + feeDTO.getClassLevelId()));
            fee.setClassLevel(classLevel);
        }

        ClassBlock classBlock = null;
        if (feeDTO.getSubClassId() != null) {
            classBlock = classBlockRepository.findByIdAndClassLevelId(feeDTO.getSubClassId(), classLevel.getId())
                    .orElseThrow(() -> new CustomNotFoundException("Student Class not found with ID: " + feeDTO.getSubClassId()));
            fee.setSubClass(classBlock);
        }

        StudentTerm studentTerm = null;
        if (feeDTO.getTermId() != null) {
            studentTerm = studentTermRepository.findById(feeDTO.getTermId())
                    .orElseThrow(() -> new CustomNotFoundException("Student term not found with ID: " + feeDTO.getTermId()));
            fee.setTerm(studentTerm);
        }

        // Validate relationships
        if (classBlock != null && classLevel != null) {
            if (!classBlock.getClassLevel().getId().equals(classLevel.getId())) {
                throw new BadRequestException("Subclass does not belong to the specified class level");
            }
        }

        if (studentTerm != null && academicSession != null) {
            if (!studentTerm.getAcademicSession().getId().equals(academicSession.getId())) {
                throw new BadRequestException("Term does not belong to the specified academic session");
            }
        }

        Fee savedFee = feeRepository.save(fee);
        logger.info("Created fee ID {} for category ID {}", savedFee.getId(), feeDTO.getCategoryId());

        // Map Fee to FeeResponseDto
        return FeeResponseDto.builder()
                .id(savedFee.getId())
                .categoryName(savedFee.getCategory().getName())
                .description(savedFee.getDescription())
                .amount(savedFee.getAmount())
                .isCompulsory(savedFee.isCompulsory())
                .schoolId(savedFee.getSchool().getId())
                .sessionId(savedFee.getSession().getId())
                .classLevelId(savedFee.getClassLevel() != null ? savedFee.getClassLevel().getId() : null)
                .subClassId(savedFee.getSubClass() != null ? savedFee.getSubClass().getId() : null)
                .termId(savedFee.getTerm() != null ? savedFee.getTerm().getId() : null)
                .active(savedFee.isActive())
                .archived(savedFee.isArchived())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentFeeResponse> getApplicableFeesForStudent(Long studentId) {
        // Fetch student profile
        Profile student = profileRepository.findById(studentId)
                .orElseThrow(() -> new CustomNotFoundException("Student profile not found with ID: " + studentId));

        // Retrieve student's class and session details
        ClassBlock classBlock = student.getSessionClass().getClassBlock();
        ClassLevel classLevel = classBlock != null ? classBlock.getClassLevel() : null;
//        AcademicSession academicSession = classLevel != null ? classLevel.getAcademicYear() : null;
        AcademicSession academicSession = null;
        School school = academicSession != null ? academicSession.getSchool() : null;

        // Get current term for the student
        StudentTerm currentTerm = getCurrentTerm(student);

        // Fetch applicable fees
        List<Fee> applicableFees = feeRepository.findApplicableFees(
                school != null ? school.getId() : null,
                academicSession != null ? academicSession.getId() : null,
                classLevel != null ? classLevel.getId() : null,
                classBlock != null ? classBlock.getId() : null,
                currentTerm != null ? currentTerm.getId() : null
        );

        // Transform Fee entities to StudentFeeResponse DTOs
        return applicableFees.stream()
                .map(fee -> {
                    // Assume a method or repository to fetch payment details for this fee and student
                    // For example: StudentFee studentFee = studentFeeRepository.findByStudentIdAndFeeId(studentId, fee.getId());
                    BigDecimal amountPaid = BigDecimal.ZERO; // Placeholder: Fetch from payment records
                    BigDecimal balance = fee.getAmount(); // Placeholder: Calculate as amount - amountPaid
                    boolean isPaid = amountPaid.compareTo(fee.getAmount()) >= 0;
                    FeeStatus feeStatus = isPaid ? FeeStatus.PAID : FeeStatus.UNPAID;

                    return StudentFeeResponse.builder()
                            .id(fee.getId()) // Set if there's a specific ID for the student-fee relation
                            .studentId(studentId)
                            .feeId(fee.getId())
//                            .dueDate(LocalDate.now().plusDays(30)) // Placeholder: Set based on term or fee policy
                            .status(feeStatus)
                            .totalAmount(fee.getAmount())
                            .amountPaid(amountPaid)
                            .balance(balance)
                            .feeName(fee.getDescription())
                            .amount(fee.getAmount())
                            .compulsory(fee.isCompulsory())
                            .paid(isPaid)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getApplicationFee(Long schoolId, Long classLevelId, Long subClassId) {
        List<Fee> fees = feeRepository.findApplicationFeeBySchool(schoolId, classLevelId, subClassId, null);

        BigDecimal amount = fees.stream()
                .filter(Fee::isCurrentlyActive)
                .findFirst()
                .map(Fee::getAmount)
                .orElse(BigDecimal.ZERO);

        logger.info("Retrieved application fee {} for school ID {}, classLevel ID {}, subClass ID {}",
                amount, schoolId, classLevelId, subClassId);
        return amount;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentFeeResponse> getApplicableUnpaidFeesForStudent(Long studentId) {
        Profile student = profileRepository.findById(studentId)
                .orElseThrow(() -> new CustomNotFoundException("Student not found with ID: " + studentId));

        StudentTerm currentTerm = getCurrentTerm(student);

//        List<Fee> applicableFees = feeRepository.findApplicableFees(
//                student.getUser().getSchool().getId(),
//                student.getClassBlock().getClassLevel().getAcademicYear().getId(),
//                student.getClassBlock().getClassLevel().getId(),
//                student.getClassBlock().getId(),
//                currentTerm != null ? currentTerm.getId() : null
//        );
        List<Fee> applicableFees = null;

        List<StudentFeeResponse> responses = applicableFees.stream()
                .map(fee -> {
                    BigDecimal totalPaid = getTotalPaymentsForFee(student, fee);
                    BigDecimal balance = fee.getAmount().subtract(totalPaid);

                    if (balance.compareTo(BigDecimal.ZERO) <= 0) {
                        return null; // Skip paid fees
                    }

                    return StudentFeeResponse.builder()
                            .feeId(fee.getId())
                            .feeName(fee.getCategory().getName())
                            .amount(fee.getAmount())
                            .amountPaid(totalPaid)
                            .balance(balance)
                            .status(totalPaid.compareTo(BigDecimal.ZERO) > 0 ? FeeStatus.PARTIALLY_PAID : FeeStatus.UNPAID)
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        logger.info("Retrieved {} unpaid fees for student ID {}", responses.size(), studentId);
        return responses;
    }

    private StudentTerm getCurrentTerm(Profile student) {
//        if (student.getClassBlock() == null ||
//                student.getClassBlock().getClassLevel() == null ||
//                student.getClassBlock().getClassLevel().getAcademicYear() == null) {
//            return null;
//        }
//
//        List<StudentTerm> terms = studentTermRepository.findByAcademicSessionOrderByStartDateAsc(
//                student.getClassBlock().getClassLevel().getAcademicYear()
//        );

//        return terms.stream()
//                .filter(term -> !term.getStartDate().isAfter(LocalDate.now()))
//                .filter(term -> !term.getEndDate().isBefore(LocalDate.now()))
//                .findFirst()
//                .orElse(null);
        return null;
    }

    private BigDecimal getTotalPaymentsForFee(Profile student, Fee fee) {
        return paymentRepository.findByProfileAndStudentFee(student, fee).stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validateFeeDTO(FeeDTO feeDTO) {
        if (feeDTO == null) {
            throw new BadRequestException("Fee data cannot be null");
        }
        if (feeDTO.getCategoryId() == null) {
            throw new BadRequestException("Fee category ID is required");
        }
        if (feeDTO.getAmount() == null || feeDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Fee amount must be positive");
        }
        if (feeDTO.getSessionId() == null) {
            throw new BadRequestException("Academic session ID is required");
        }
    }
}
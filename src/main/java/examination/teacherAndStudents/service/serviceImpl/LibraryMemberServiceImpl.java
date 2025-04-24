package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.LibraryMemberResponse;
import examination.teacherAndStudents.dto.LibraryMembershipRequest;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.*;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.LibraryMemberService;
import examination.teacherAndStudents.utils.AccountUtils;
import examination.teacherAndStudents.utils.BorrowingStatus;
import examination.teacherAndStudents.utils.MembershipStatus;
import examination.teacherAndStudents.utils.Roles;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LibraryMemberServiceImpl implements LibraryMemberService {

    private static final Logger logger = LoggerFactory.getLogger(LibraryMemberServiceImpl.class);

    private final LibraryMemberRepository libraryMemberRepository;
    private final ProfileRepository profileRepository;
    private final ClassBlockRepository classBlockRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository; // Assumed from LibraryServiceImpl
    private final BookBorrowingRepository bookBorrowingRepository;

    @Override
    @Transactional
    public LibraryMemberResponse createLibraryMember(LibraryMembershipRequest request) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));

        Profile student = profileRepository.findByUniqueRegistrationNumber(request.getUserUniqueRegistrationNumber())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with unique registration number: " + request.getUserUniqueRegistrationNumber()));

        // Optional: Validate class if provided
        if (request.getUserClassId() != null) {
            ClassBlock studentClass = classBlockRepository.findById(request.getUserClassId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Class not found with ID: " + request.getUserClassId()));
            // Add class-specific logic if needed (e.g., ensure student belongs to class)
        }

        // Check for existing active membership
        if (libraryMemberRepository.existsByStudentAndStatus(student, MembershipStatus.ACTIVE)) {
            throw new EntityAlreadyExistException("Student already has an active library membership");
        }

        String membershipId = AccountUtils.generateLibraryId();
        LibraryMembership libraryMember = LibraryMembership.builder()
                .memberId(membershipId)
                .student(student)
                .status(MembershipStatus.ACTIVE)
                .expiryDate(request.getExpiryDate() != null ? request.getExpiryDate() : LocalDateTime.now().plusYears(1))
                .build();

        LibraryMembership savedMember = libraryMemberRepository.save(libraryMember);

        auditLogRepository.save(
                AuditLog.builder()
                        .action("CREATE_LIBRARY_MEMBER")
                        .performedBy(libraryMember.getStudent())
                        .timestamp(LocalDateTime.now())
                        .details("Member ID: " + membershipId + ", Student: " + student.getUniqueRegistrationNumber())
                        .build()
        );

        logger.info("Created library membership for student: {}", student.getUniqueRegistrationNumber());
        return mapToLibraryMemberResponse(savedMember);
    }

    @Override
    @Transactional
    public LibraryMemberResponse updateLibraryMember(Long memberId, LibraryMembershipRequest request) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));

        LibraryMembership existingMember = libraryMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Library membership not found with ID: " + memberId));

        // Update fields if provided
        if (request.getUserUniqueRegistrationNumber() != null) {
            Profile newStudent = profileRepository.findByUniqueRegistrationNumber(request.getUserUniqueRegistrationNumber())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User not found with unique registration number: " + request.getUserUniqueRegistrationNumber()));
            existingMember.setStudent(newStudent);
        }

        if (request.getStatus() != null) {
            existingMember.setStatus(request.getStatus());
        }

        if (request.getExpiryDate() != null) {
            if (request.getExpiryDate().isBefore(LocalDateTime.now())) {
                throw new BadRequestException("Expiry date cannot be in the past");
            }
            existingMember.setExpiryDate(request.getExpiryDate());
        }

        LibraryMembership updatedMember = libraryMemberRepository.save(existingMember);

        auditLogRepository.save(
                AuditLog.builder()
                        .action("UPDATE_LIBRARY_MEMBER")
                        .performedBy(existingMember.getStudent())
                        .timestamp(LocalDateTime.now())
                        .details("Member ID: " + existingMember.getMemberId())
                        .build()
        );

        logger.info("Updated library membership ID: {}", memberId);
        return mapToLibraryMemberResponse(updatedMember);
    }

    @Override
    public LibraryMemberResponse findById(Long id) {
        LibraryMembership libraryMember = libraryMemberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Library member not found with ID: " + id));
        return mapToLibraryMemberResponse(libraryMember);
    }

    @Override
    public Page<LibraryMemberResponse> findAll(int pageNo, int pageSize, String sortBy, String sortDirection) {
        try {
            sortBy = (sortBy == null || sortBy.isEmpty()) ? "id" : sortBy;
            Sort.Direction direction = Sort.Direction.fromString(sortDirection != null ? sortDirection : "ASC");
            Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(direction, sortBy));
            Page<LibraryMembership> libraryMembers = libraryMemberRepository.findAll(paging);
            return libraryMembers.map(this::mapToLibraryMemberResponse);
        } catch (Exception e) {
            throw new CustomInternalServerException("Error fetching library members: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteLibraryMember(Long id) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));

        LibraryMembership member = libraryMemberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Library member not found with ID: " + id));

        // Check for active borrowings
        if (bookBorrowingRepository.existsByStudentProfileAndStatusNot(
                member.getStudent(), BorrowingStatus.RETURNED)) {
            throw new BadRequestException("Cannot delete membership with active borrowings");
        }

        libraryMemberRepository.deleteById(id);

        auditLogRepository.save(
                AuditLog.builder()
                        .action("DELETE_LIBRARY_MEMBER")
                        .performedBy(member.getStudent())
                        .timestamp(LocalDateTime.now())
                        .details("Member ID: " + member.getMemberId())
                        .build()
        );

        logger.info("Deleted library membership ID: {}", id);
    }

    private LibraryMemberResponse mapToLibraryMemberResponse(LibraryMembership libraryMember) {
        return LibraryMemberResponse.builder()
                .id(libraryMember.getId())
                .memberId(libraryMember.getMemberId())
                .studentUniqueRegistrationNumber(libraryMember.getStudent().getUniqueRegistrationNumber())
                .studentName(libraryMember.getStudent().getUser().getFirstName() + " " + libraryMember.getStudent().getUser().getLastName()) // Assumes User has fullName
                .status(libraryMember.getStatus())
                .joinDate(libraryMember.getJoinDate())
                .expiryDate(libraryMember.getExpiryDate())
                .createdAt(libraryMember.getCreatedAt())
                .updatedAt(libraryMember.getUpdatedAt())
                .build();
    }

    @Transactional
    public void suspendMembership(Long memberId, String reason) {
        LibraryMembership membership = libraryMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Library membership not found with ID: " + memberId));
        membership.setStatus(MembershipStatus.SUSPENDED);
        libraryMemberRepository.save(membership);
        auditLogRepository.save(
                AuditLog.builder()
                        .action("SUSPEND_LIBRARY_MEMBER")
                        .performedBy(membership.getStudent())
                        .timestamp(LocalDateTime.now())
                        .details("Member ID: " + membership.getMemberId() + ", Reason: " + reason)
                        .build()
        );
        logger.info("Suspended membership ID: {}", memberId);
    }


    @Transactional
    @Scheduled(cron = "0 0 0 * * ?") // Daily at midnight
    public void updateExpiredMemberships() {
        List<LibraryMembership> memberships = libraryMemberRepository.findByStatusAndExpiryDateBefore(
                MembershipStatus.ACTIVE, LocalDateTime.now());
        memberships.forEach(membership -> {
            membership.setStatus(MembershipStatus.EXPIRED);
            libraryMemberRepository.save(membership);
        });
        logger.info("Updated {} expired memberships", memberships.size());
    }


}
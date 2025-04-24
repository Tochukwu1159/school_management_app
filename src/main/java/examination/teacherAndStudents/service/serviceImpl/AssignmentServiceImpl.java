package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.AssignmentFilter;
import examination.teacherAndStudents.dto.AssignmentRequest;
import examination.teacherAndStudents.dto.AssignmentResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.AssignmentService;
import jakarta.persistence.criteria.Join;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final ProfileRepository profileRepository;
    private final SubjectRepository subjectRepository;
    private final ClassBlockRepository classBlockRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AssignmentResponse saveAssignment(AssignmentRequest request) {
        Profile teacher = profileRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new RuntimeException("Teacher profile not found"));

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        // Create the assignment
        Assignment assignment = Assignment.builder()
                .teacher(teacher)
                .subject(subject)
                .description(request.getDescription())
                .attachment(request.getAttachment())
                .dateIssued(request.getDateIssued())
                .dateDue(request.getDateDue())
                .totalMark(request.getTotalMark())
                .instructions(request.getInstructions())
                .title(request.getTitle())
                .build();

        // Save the assignment first to generate ID
        assignment = assignmentRepository.save(assignment);

        // Handle class blocks
        if (request.getClassIds() != null && !request.getClassIds().isEmpty()) {
            Set<ClassBlock> classBlocks = new HashSet<>(classBlockRepository.findAllById(request.getClassIds()));

            if (classBlocks.size() != request.getClassIds().size()) {
                throw new RuntimeException("One or more class blocks not found");
            }

            // Add all class blocks to the assignment
            classBlocks.forEach(assignment::addClassBlock);
        }

        return toResponse(assignment);
    }

    @Override
    @Transactional
    public AssignmentResponse updateAssignment(Long id, AssignmentRequest request) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        // Validate and fetch required entities
        Profile teacher = profileRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new RuntimeException("Teacher profile not found"));

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        // Update basic assignment info
        assignment.setTeacher(teacher);
        assignment.setSubject(subject);
        assignment.setDescription(request.getDescription());
        assignment.setAttachment(request.getAttachment());
        assignment.setDateIssued(request.getDateIssued());
        assignment.setDateDue(request.getDateDue());
        assignment.setTotalMark(request.getTotalMark());
        assignment.setInstructions(request.getInstructions());
        assignment.setTitle(request.getTitle());

        // Handle class blocks update
        if (request.getClassIds() != null) {
            // Clear existing class blocks
            assignment.getClassBlocks().clear();

            // Add new ones if provided
            if (!request.getClassIds().isEmpty()) {
                Set<ClassBlock> classBlocks = new HashSet<>(classBlockRepository.findAllById(request.getClassIds()));

                if (classBlocks.size() != request.getClassIds().size()) {
                    throw new RuntimeException("One or more class blocks not found");
                }

                classBlocks.forEach(assignment::addClassBlock);
            }
        }

        assignment = assignmentRepository.save(assignment);
        return toResponse(assignment);
    }


    @Override
    public AssignmentResponse getAssignmentById(Long id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        return toResponse(assignment);
    }

    @Override
    public Page<AssignmentResponse> getAllAssignments(AssignmentFilter filter, Pageable pageable) {
        Specification<Assignment> spec = Specification.where(null);

        if (filter != null) {
            if (filter.getTeacherId() != null) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("teacher").get("id"), filter.getTeacherId()));
            }
            if (filter.getSubjectId() != null) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("subject").get("id"), filter.getSubjectId()));
            }
            if (filter.getTitle() != null) {
                spec = spec.and((root, query, cb) ->
                        cb.like(cb.lower(root.get("title")), "%" + filter.getTitle().toLowerCase() + "%"));
            }
            if (filter.getDateIssuedFrom() != null) {
                spec = spec.and((root, query, cb) ->
                        cb.greaterThanOrEqualTo(root.get("dateIssued"), filter.getDateIssuedFrom()));
            }
            if (filter.getDateIssuedTo() != null) {
                spec = spec.and((root, query, cb) ->
                        cb.lessThanOrEqualTo(root.get("dateIssued"), filter.getDateIssuedTo()));
            }
            if (filter.getDateDueFrom() != null) {
                spec = spec.and((root, query, cb) ->
                        cb.greaterThanOrEqualTo(root.get("dateDue"), filter.getDateDueFrom()));
            }
            if (filter.getDateDueTo() != null) {
                spec = spec.and((root, query, cb) ->
                        cb.lessThanOrEqualTo(root.get("dateDue"), filter.getDateDueTo()));
            }
            if (filter.getClassBlockId() != null) {
                spec = spec.and((root, query, cb) -> {
                    Join<Assignment, ClassBlock> classBlockJoin = root.join("classBlocks");
                    return cb.equal(classBlockJoin.get("id"), filter.getClassBlockId());
                });
            }
        }

        Page<Assignment> assignments = assignmentRepository.findAll(spec, pageable);
        return assignments.map(this::toResponse);
    }

    @Transactional
    public void deleteAssignment(Long id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        new HashSet<>(assignment.getClassBlocks()).forEach(assignment::removeClassBlock);

        assignmentRepository.delete(assignment);
    }
    private AssignmentResponse toResponse(Assignment assignment) {
        User teacherUser = userRepository.findById(assignment.getTeacher().getUser().getId())
                .orElseThrow(() -> new RuntimeException("Teacher user not found"));

        return AssignmentResponse.builder()
                .id(assignment.getId())
                .teacherId(assignment.getTeacher().getId())
                .teacherName(teacherUser.getFirstName() + " " + teacherUser.getLastName())
                .subjectId(assignment.getSubject().getId())
                .subjectName(assignment.getSubject().getName())
                .classIds(assignment.getClassBlocks().stream()
                        .map(ClassBlock::getId)
                        .collect(Collectors.toList()))
                .classNames(assignment.getClassBlocks().stream()
                        .map(ClassBlock::getName)
                        .collect(Collectors.toList()))
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .instructions(assignment.getInstructions())
                .attachment(assignment.getAttachment())
                .totalMark(assignment.getTotalMark())
                .dateIssued(assignment.getDateIssued())
                .dateDue(assignment.getDateDue())
                .build();
    }
}
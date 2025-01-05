package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.AssignmentRequest;
import examination.teacherAndStudents.dto.AssignmentResponse;
import examination.teacherAndStudents.entity.Assignment;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.Subject;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.repository.AssignmentRepository;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.repository.SubjectRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final ProfileRepository profileRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    @Override
    public AssignmentResponse saveAssignment(AssignmentRequest request) {
        Profile teacher = profileRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        Profile profile = profileRepository.findById(request.getProfileId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        Assignment assignment = new Assignment();
        assignment.setTeacher(teacher);
        assignment.setStudent(profile);
        assignment.setSubject(subject);
        assignment.setDescription(request.getDescription());
        assignment.setAttachment(request.getAttachment());
        assignment.setDateIssued(request.getDateIssued());
        assignment.setDateDue(request.getDateDue());

        assignment = assignmentRepository.save(assignment);

        return toResponse(assignment);
    }

    @Override
    public AssignmentResponse updateAssignment(Long id, AssignmentRequest request) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        Profile teacher = profileRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        Profile profile = profileRepository.findById(request.getProfileId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        assignment.setTeacher(teacher);
        assignment.setStudent(profile);
        assignment.setSubject(subject);
        assignment.setDescription(request.getDescription());
        assignment.setAttachment(request.getAttachment());
        assignment.setDateIssued(request.getDateIssued());
        assignment.setDateDue(request.getDateDue());

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
    public List<AssignmentResponse> getAllAssignments() {
        List<Assignment> assignments = assignmentRepository.findAll();
        return assignments.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAssignment(Long id) {
        if (!assignmentRepository.existsById(id)) {
            throw new RuntimeException("Assignment not found");
        }
        assignmentRepository.deleteById(id);
    }

    private AssignmentResponse toResponse(Assignment assignment) {
        User teacher = userRepository.findById(assignment.getTeacher().getUser().getId())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        User student = userRepository.findById(assignment.getStudent().getUser().getId())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return AssignmentResponse.builder()
                .id(assignment.getId())
                .teacherId(assignment.getTeacher().getId())
                .teacherName(teacher.getFirstName() + " " + teacher.getLastName())
                .profileId(assignment.getStudent().getId())
                .profileName(student.getFirstName() + " " + teacher.getLastName())
                .subjectId(assignment.getSubject().getId())
                .subjectName(assignment.getSubject().getName())
                .description(assignment.getDescription())
                .attachment(assignment.getAttachment())
                .dateIssued(assignment.getDateIssued())
                .dateDue(assignment.getDateDue())
                .build();
    }
}

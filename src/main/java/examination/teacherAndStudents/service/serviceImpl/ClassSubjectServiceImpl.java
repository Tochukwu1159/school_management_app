package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.ClassSubjectRequest;
import examination.teacherAndStudents.dto.ClassSubjectResponse;
import examination.teacherAndStudents.entity.ClassSubject;
import examination.teacherAndStudents.entity.Subject;
import examination.teacherAndStudents.entity.ClassBlock;
import examination.teacherAndStudents.entity.AcademicSession;
import examination.teacherAndStudents.repository.ClassSubjectRepository;
import examination.teacherAndStudents.repository.SubjectRepository;
import examination.teacherAndStudents.repository.ClassBlockRepository;
import examination.teacherAndStudents.repository.AcademicSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassSubjectServiceImpl implements ClassSubjectService {

    private final ClassSubjectRepository classSubjectRepository;
    private final SubjectRepository subjectRepository;
    private final ClassBlockRepository classBlockRepository;
    private final AcademicSessionRepository academicSessionRepository;

    public ClassSubjectResponse saveClassSubject(ClassSubjectRequest request) {
        // Fetch the required entities from the repositories
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject with id " + request.getSubjectId() + " not found"));

        ClassBlock classBlock = classBlockRepository.findById(request.getClassBlockId())
                .orElseThrow(() -> new RuntimeException("ClassBlock with id " + request.getClassBlockId() + " not found"));

        AcademicSession academicSession = academicSessionRepository.findById(request.getAcademicYearId())
                .orElseThrow(() -> new RuntimeException("AcademicSession with id " + request.getAcademicYearId() + " not found"));

        // Create or update the ClassSubject entity
        ClassSubject classSubject = new ClassSubject();
        classSubject.setSubject(subject);
        classSubject.setClassBlock(classBlock);
        classSubject.setTerm(request.getTerm());
        classSubject.setAcademicYear(academicSession);
        classSubject.setCreatedAt(LocalDateTime.now());
        classSubject.setUpdatedAt(LocalDateTime.now());

        classSubject = classSubjectRepository.save(classSubject);
        ClassSubjectResponse response = new ClassSubjectResponse();
        response.setId(classSubject.getId());
        response.setSubject(classSubject.getSubject());
        response.setClassBlock(classSubject.getClassBlock());
        response.setTerm(classSubject.getTerm());
        response.setAcademicYear(classSubject.getAcademicYear());
        response.setCreatedAt(classSubject.getCreatedAt());
        response.setUpdatedAt(classSubject.getUpdatedAt());

        return response;
    }
    public ClassSubjectResponse getClassSubjectById(Long id) {
        Optional<ClassSubject> classSubjectOptional = classSubjectRepository.findById(id);
        if (classSubjectOptional.isPresent()) {
            ClassSubject classSubject = classSubjectOptional.get();
            return convertToClassSubjectResponse(classSubject);
        } else {
            throw new RuntimeException("ClassSubject with id " + id + " not found");
        }
    }

    public List<ClassSubjectResponse> getAllClassSubjects() {
        List<ClassSubject> classSubjects = classSubjectRepository.findAll();
        return classSubjects.stream()
                .map(this::convertToClassSubjectResponse)
                .collect(Collectors.toList());
    }

    public void deleteClassSubject(Long id) {
        Optional<ClassSubject> classSubjectOptional = classSubjectRepository.findById(id);
        if (classSubjectOptional.isPresent()) {
            classSubjectRepository.deleteById(id);
        } else {
            throw new RuntimeException("ClassSubject with id " + id + " not found");
        }
    }

    // Helper method to convert ClassSubject entity to ClassSubjectResponse DTO
    private ClassSubjectResponse convertToClassSubjectResponse(ClassSubject classSubject) {
        ClassSubjectResponse response = new ClassSubjectResponse();
        response.setId(classSubject.getId());
        response.setSubject(classSubject.getSubject());
        response.setClassBlock(classSubject.getClassBlock());
        response.setTerm(classSubject.getTerm());
        response.setAcademicYear(classSubject.getAcademicYear());
        response.setCreatedAt(classSubject.getCreatedAt());
        response.setUpdatedAt(classSubject.getUpdatedAt());
        return response;
    }
}


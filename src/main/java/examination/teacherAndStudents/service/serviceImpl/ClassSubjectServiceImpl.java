package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.EntityAlreadyExistException;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.ClassSubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
                .orElseThrow(() -> new NotFoundException("Subject with id " + request.getSubjectId() + " not found"));

        ClassBlock classBlock = classBlockRepository.findById(request.getClassBlockId())
                .orElseThrow(() -> new NotFoundException("ClassBlock with id " + request.getClassBlockId() + " not found"));

        AcademicSession academicSession = academicSessionRepository.findById(request.getAcademicYearId())
                .orElseThrow(() -> new NotFoundException("AcademicSession with id " + request.getAcademicYearId() + " not found"));

        // Check if the subject already exists for the class block and academic year
        boolean exists = classSubjectRepository.existsBySubjectAndClassBlockAndAcademicYear(
                subject, classBlock, academicSession
        );
        if (exists) {
            throw new EntityAlreadyExistException("Subject already added for this class and academic year");
        }

        // Create or update the ClassSubject entity
        ClassSubject classSubject = new ClassSubject();
        classSubject.setSubject(subject);
        classSubject.setClassBlock(classBlock);
        classSubject.setAcademicYear(academicSession);
        classSubject.setCreatedAt(LocalDateTime.now());
        classSubject.setUpdatedAt(LocalDateTime.now());

        classSubject = classSubjectRepository.save(classSubject);

        return toResponse(classSubject);
    }
    public ClassSubjectResponse getClassSubjectById(Long id) {
        Optional<ClassSubject> classSubjectOptional = classSubjectRepository.findById(id);
        if (classSubjectOptional.isPresent()) {
            ClassSubject classSubject = classSubjectOptional.get();
            return toResponse(classSubject);
        } else {
            throw new RuntimeException("ClassSubject with id " + id + " not found");
        }
    }

    public Page<ClassSubjectResponse> getAllClassSubjects(
            Long academicYearId,
            Long subjectId,
            Long classSubjectId,
            String subjectName,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        // Create Pageable object
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // Fetch filtered and paginated results
        Page<ClassSubject> classSubjects = classSubjectRepository.findAllWithFilters(
                academicYearId,
                subjectId,
                classSubjectId,
                subjectName,
                pageable);

        // Map to response DTO
        return classSubjects.map(this::toResponse);
    }

    public void deleteClassSubject(Long id) {
        Optional<ClassSubject> classSubjectOptional = classSubjectRepository.findById(id);
        if (classSubjectOptional.isPresent()) {
            classSubjectRepository.deleteById(id);
        } else {
            throw new RuntimeException("ClassSubject with id " + id + " not found");
        }
    }

    public ClassSubjectResponse toResponse(ClassSubject classSubject) {
        return ClassSubjectResponse.builder()
                .id(classSubject.getId())
                .subject(new SubjectResponse(
                classSubject.getSubject().getId(),
                classSubject.getSubject().getName()
        ))
                .classBlock(new ClassBlockResponses(
                        classSubject.getClassBlock().getId(),
                        classSubject.getClassBlock().getCurrentStudentClassName(),
                        new ClassLevelResponse(
                                classSubject.getClassBlock().getClassLevel().getId(),
                                classSubject.getClassBlock().getClassLevel().getClassName()
                        )
                ))
                .academicYear(new AcademicSessionResponse(
                        classSubject.getAcademicYear().getId(),
                        classSubject.getAcademicYear().getName()
                ))
                .createdAt(classSubject.getCreatedAt())
                .updatedAt(classSubject.getUpdatedAt())
                .build();
    }
}


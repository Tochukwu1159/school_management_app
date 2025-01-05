package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.CurriculumRequest;
import examination.teacherAndStudents.dto.CurriculumResponse;
import examination.teacherAndStudents.entity.ClassSubject;
import examination.teacherAndStudents.entity.Curriculum;
import examination.teacherAndStudents.entity.Subject;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.repository.ClassSubjectRepository;
import examination.teacherAndStudents.repository.CurriculumRepository;
import examination.teacherAndStudents.repository.SubjectRepository;
import examination.teacherAndStudents.service.CurriculumService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurriculumServiceImpl implements CurriculumService {

    private final CurriculumRepository curriculumRepository;
    private final SubjectRepository subjectRepository;
    private final ClassSubjectRepository classSubjectRepository;

    public CurriculumResponse addCurriculumToClassSubject(Long classSubjectId, CurriculumRequest request) {
        // Fetch the ClassSubject
        ClassSubject classSubject = classSubjectRepository.findById(classSubjectId)
                .orElseThrow(() -> new NotFoundException("ClassSubject with id " + classSubjectId + " not found"));

        // Fetch the subject for the curriculum
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new NotFoundException("Subject with id " + request.getSubjectId() + " not found"));

        // Create the Curriculum
        Curriculum curriculum = new Curriculum();
        curriculum.setDescription(request.getDescription());
        curriculum.setResources(request.getResources());
        curriculum.setSubject(subject);
        curriculum.setClassSubject(classSubject);

        curriculum = curriculumRepository.save(curriculum);

        return toResponse(curriculum);
    }

    private CurriculumResponse toResponse(Curriculum curriculum) {
        return CurriculumResponse.builder()
                .id(curriculum.getId())
                .description(curriculum.getDescription())
                .resources(curriculum.getResources())
                .subjectId(curriculum.getSubject().getId())
                .subjectName(curriculum.getSubject().getName())
                .build();
    }
}

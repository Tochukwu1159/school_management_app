package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.CurriculumRequest;
import examination.teacherAndStudents.dto.CurriculumResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.repository.CurriculumRepository;
import examination.teacherAndStudents.service.CurriculumService;
import examination.teacherAndStudents.utils.EntityFetcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CurriculumServiceImpl implements CurriculumService {

    private final CurriculumRepository curriculumRepository;
    private final EntityFetcher entityFetcher;

    @Override
    public CurriculumResponse addCurriculumToClassSubject(Long classSubjectId, CurriculumRequest request) {
        ClassSubject classSubject = entityFetcher.fetchClassSubject(classSubjectId);
        ClassBlock classBlock = classSubject.getClassBlock();
        StudentTerm studentTerm = entityFetcher.fetchStudentTerm(request.getTermId());
        String email = entityFetcher.fetchLoggedInUser();
        User teacher = entityFetcher.fetchUserFromEmail(email);
        Profile techerProfile = entityFetcher.fetchProfileByUser(teacher);

        Curriculum curriculum = Curriculum.builder()
                .title(request.getTitle())
                .week(request.getWeek())
                .classBlock(classBlock)
                .term(studentTerm)
                .teacher(techerProfile)
                .description(request.getDescription())
                .resources(request.getResources())
                .classSubject(classSubject)
                .build();

        curriculum = curriculumRepository.save(curriculum);
        return toResponse(curriculum);
    }

    @Override
    public CurriculumResponse updateCurriculum(Long curriculumId, CurriculumRequest request) {
        Curriculum curriculum = entityFetcher.fetchCurriculum(curriculumId);

        curriculum.setTitle(request.getTitle());
        curriculum.setDescription(request.getDescription());
        curriculum.setResources(request.getResources());
        curriculum = curriculumRepository.save(curriculum);
        return toResponse(curriculum);
    }

    @Override
    public CurriculumResponse getCurriculumById(Long curriculumId) {
        Curriculum curriculum = entityFetcher.fetchCurriculum(curriculumId);
        return toResponse(curriculum);
    }

    @Override
    public List<CurriculumResponse> getAllCurriculums() {
        List<Curriculum> curriculums = curriculumRepository.findAll();
        return curriculums.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteCurriculum(Long curriculumId) {
        Curriculum curriculum = entityFetcher.fetchCurriculum(curriculumId);
        curriculumRepository.delete(curriculum);
    }

    private CurriculumResponse toResponse(Curriculum curriculum) {
        return CurriculumResponse.builder()
                .id(curriculum.getId())
                .title(curriculum.getTitle())
                .description(curriculum.getDescription())
                .resources(curriculum.getResources())
                .classSubjectId(curriculum.getClassSubject().getId())
                .build();
    }
}

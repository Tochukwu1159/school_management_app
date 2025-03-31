package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.StudyMaterialRequest;
import examination.teacherAndStudents.dto.StudyMaterialResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.StudyMaterialService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyMaterialServiceImpl implements StudyMaterialService {

    private final StudyMaterialRepository studyMaterialRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final AcademicSessionRepository academicSessionRepository;
    private final StudentTermRepository studentTermRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<StudyMaterialResponse> getAllMaterials() {
        return studyMaterialRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public StudyMaterialResponse getMaterialById(Long id) {
        StudyMaterial material = studyMaterialRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Study material not found with ID: " + id));
        return toResponse(material);
    }


    @Override
    public StudyMaterialResponse saveMaterial(StudyMaterialRequest request) {
        // Validate Subject
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new CustomNotFoundException("Subject not found with ID: " + request.getSubjectId()));

        // Validate Teacher
        User teacher = userRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new CustomNotFoundException("Teacher not found with ID: " + request.getTeacherId()));

        // Validate Academic Year
        AcademicSession academicYear = academicSessionRepository.findById(request.getAcademicYearId())
                .orElseThrow(() -> new CustomNotFoundException("Academic Year not found with ID: " + request.getAcademicYearId()));

        // Validate Student Term
        StudentTerm term = studentTermRepository.findById(request.getTermId())
                .orElseThrow(() -> new CustomNotFoundException("Student Term not found with ID: " + request.getTermId()));

        // Create and save StudyMaterial
        StudyMaterial material = StudyMaterial.builder()
                .title(request.getTitle())
                .filePath(request.getFilePath())
                .subject(subject)
                .teacher(teacher)
                .academicYear(academicYear)
                .studentTerm(term)
                .build();

        studyMaterialRepository.save(material);
        return toResponse(material);
    }

    @Override
    public void deleteMaterial(Long id) {
        if (!studyMaterialRepository.existsById(id)) {
            throw new CustomNotFoundException("Study material not found with ID: " + id);
        }
        studyMaterialRepository.deleteById(id);
    }

    private StudyMaterialResponse toResponse(StudyMaterial material) {
        return modelMapper.map(material, StudyMaterialResponse.class);
    }
}

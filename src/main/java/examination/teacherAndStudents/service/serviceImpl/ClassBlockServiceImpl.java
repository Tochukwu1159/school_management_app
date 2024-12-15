package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.ClassBlockRequest;
import examination.teacherAndStudents.dto.ClassBlockResponse;
import examination.teacherAndStudents.entity.ClassBlock;
import examination.teacherAndStudents.entity.ClassLevel;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.ResourceNotFoundException;
import examination.teacherAndStudents.repository.ClassBlockRepository;
import examination.teacherAndStudents.repository.ClassLevelRepository;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.ClassBlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassBlockServiceImpl implements ClassBlockService {

    private final ClassBlockRepository classBlockRepository;
    private final ClassLevelRepository classLevelRepository;
    private final ProfileRepository profileRepository;

    public ClassBlockResponse createClassBlock(ClassBlockRequest request) {
        ClassLevel classLevel = classLevelRepository.findById(request.getClassLevelId())
                .orElseThrow(() -> new ResourceNotFoundException("Class Level not found"));
        System.out.println(classLevel);

        ClassBlock classBlock = ClassBlock.builder()
                .classLevel(classLevel)
                .currentStudentClassName(request.getSubClassName())
                .classUniqueUrl(request.getClassUniqueUrl())
                .numberOfStudents(0)
                .build();
        System.out.println(classBlock +"cccccccc");
        classBlockRepository.save(classBlock);
        return mapToResponse(classBlock);
    }

    public ClassBlockResponse getClassBlockById(Long id) {
        ClassBlock classBlock = classBlockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class Block not found"));
        return mapToResponse(classBlock);
    }

    public List<ClassBlockResponse> getAllClassBlocks() {
        return classBlockRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ClassBlockResponse updateClassBlock(Long id, ClassBlockRequest request) {
        ClassBlock classBlock = classBlockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class Block not found"));
        classBlock.setClassUniqueUrl(request.getClassUniqueUrl());
        classBlockRepository.save(classBlock);
        return mapToResponse(classBlock);
    }


    public void deleteClassBlock(Long id) {
        ClassBlock classBlock = classBlockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class Block not found"));
        classBlockRepository.delete(classBlock);
    }

    public ClassBlockResponse updateFormTeacher(Long id, Long formTeacherId) {
        ClassBlock classBlock = classBlockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class Block not found"));

        Profile newFormTeacher = profileRepository.findById(formTeacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Form Teacher not found"));

        classBlock.setFormTeacher(newFormTeacher);
        classBlockRepository.save(classBlock);
        return mapToResponse(classBlock);
    }

    private ClassBlockResponse mapToResponse(ClassBlock classBlock) {
        return ClassBlockResponse.builder()
                .id(classBlock.getId())
                .currentStudentClassName(classBlock.getCurrentStudentClassName())
                .classLevelId(classBlock.getClassLevel().getId())
                .classUniqueUrl(classBlock.getClassUniqueUrl())
                .numberOfStudents(classBlock.getNumberOfStudents())
                .term(classBlock.getTerm())
                .build();
    }
}

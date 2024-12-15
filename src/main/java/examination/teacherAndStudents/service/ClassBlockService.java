package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.ClassBlockRequest;
import examination.teacherAndStudents.dto.ClassBlockResponse;

import java.util.List;

public interface ClassBlockService {
    ClassBlockResponse createClassBlock(ClassBlockRequest request);
    ClassBlockResponse getClassBlockById(Long id);
    List<ClassBlockResponse> getAllClassBlocks();
    ClassBlockResponse updateClassBlock(Long id, ClassBlockRequest request);
    void deleteClassBlock(Long id);
    ClassBlockResponse updateFormTeacher(Long id, Long formTeacherId);
}

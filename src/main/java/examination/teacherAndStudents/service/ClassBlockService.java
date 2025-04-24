package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.ClassBlockRequest;
import examination.teacherAndStudents.dto.ClassBlockResponse;
import examination.teacherAndStudents.dto.UpdateFormTeacherRequest;

import java.util.List;

public interface ClassBlockService {
    ClassBlockResponse createClassBlock(ClassBlockRequest request);
    ClassBlockResponse getClassBlockById(Long id);
    List<ClassBlockResponse> getAllClassBlocks(
            Long classId,
            Long subClassId,
            Long academicYearId);
    ClassBlockResponse updateClassBlock(Long id, ClassBlockRequest request);
    void deleteClassBlock(Long id);
    ClassBlockResponse updateFormTeacher(UpdateFormTeacherRequest request);
    ClassBlockResponse changeStudentClass(Long studentId, ClassBlockRequest request);
}

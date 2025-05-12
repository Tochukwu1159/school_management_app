package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.ClassNameRequest;
import examination.teacherAndStudents.dto.ClassNameResponse;
import org.springframework.data.domain.Page;

public interface ClassNameService {
    ClassNameResponse createClassName(ClassNameRequest request);
    ClassNameResponse updateClassName(Long id, ClassNameRequest request);
    ClassNameResponse getClassNameById(Long id);
    Page<ClassNameResponse> getAllClassNames(String name, int page, int size, String sortBy, String sortDirection);
    void deleteClassName(Long id);
}
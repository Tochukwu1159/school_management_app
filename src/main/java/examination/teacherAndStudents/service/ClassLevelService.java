package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.ClassLevelRequest;
import examination.teacherAndStudents.dto.ClassLevelRequestUrl;
import examination.teacherAndStudents.entity.ClassLevel;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface ClassLevelService {
    Page<ClassLevel> getAllClassLevels(
            Long classLevelId,
            Long academicYearId,
            int page,
            int size,
            String sortBy,
            String sortDirection);
    ClassLevel updateClassLevelUrl(Long id, ClassLevelRequestUrl classLevelRequest);
    Optional<ClassLevel> getClassLevelById(Long id);
    ClassLevel createClassLevel(ClassLevelRequest classLevel);
    ClassLevel updateClassLevel(Long id, ClassLevelRequest updatedClassLevel);
    void deleteClassLevel(Long id);
}

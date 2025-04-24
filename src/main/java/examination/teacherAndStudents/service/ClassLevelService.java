package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.ClassLevelRequest;
import examination.teacherAndStudents.dto.ClassLevelRequestUrl;
import examination.teacherAndStudents.entity.ClassBlock;
import examination.teacherAndStudents.entity.ClassLevel;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ClassLevelService {
    Page<ClassLevel> getAllClassLevels(
            Long classLevelId,
            Long academicYearId,
            String className,
            int page,
            int size,
            String sortBy,
            String sortDirection
    );

    List<ClassBlock> getSubClassesByClassLevelId(Long classLevelId);

    ClassBlock updateClassBlockUrl(Long classBlockId, @Valid ClassLevelRequestUrl classLevelRequestUrl);

    ClassLevel getClassLevelById(Long id);

    ClassLevel createClassLevel(ClassLevelRequest classLevel); // No @Valid here

    ClassLevel updateClassLevel(Long id, ClassLevelRequest updatedClassLevel);

    void deleteClassLevel(Long id);
}
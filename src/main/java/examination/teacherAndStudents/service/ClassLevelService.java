package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.ClassLevelRequest;
import examination.teacherAndStudents.dto.ClassLevelRequestUrl;
import examination.teacherAndStudents.entity.ClassLevel;

import java.util.List;
import java.util.Optional;

public interface ClassLevelService {
    List<ClassLevel> getAllClassLevels();
     ClassLevel updateClassLevelUrl(Long id, ClassLevelRequestUrl classLevelRequest);
    Optional<ClassLevel> getClassLevelById(Long id);
    ClassLevel createClassLevel(ClassLevelRequest classLevel);
    ClassLevel updateClassLevel(Long id, ClassLevelRequest updatedClassLevel);
    void deleteClassLevel(Long id);
}

package examination.teacherAndStudents.service;

import examination.teacherAndStudents.entity.StudyMaterial;

import java.util.List;


public interface StudyMaterialService {
    List<StudyMaterial> getAllMaterials();
    void deleteMaterial(Long id);
    StudyMaterial getMaterialById(Long id);
    void saveMaterial(StudyMaterial material);



}

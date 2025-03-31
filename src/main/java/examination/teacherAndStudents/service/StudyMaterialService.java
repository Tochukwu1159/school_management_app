package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.StudyMaterialRequest;
import examination.teacherAndStudents.dto.StudyMaterialResponse;
import examination.teacherAndStudents.entity.StudyMaterial;

import java.util.List;


public interface StudyMaterialService {
    List<StudyMaterialResponse> getAllMaterials();
    void deleteMaterial(Long id);
    StudyMaterialResponse getMaterialById(Long id);
    StudyMaterialResponse saveMaterial(StudyMaterialRequest request);



}

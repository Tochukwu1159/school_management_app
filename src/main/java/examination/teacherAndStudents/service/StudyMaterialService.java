package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.StudyMaterialRequest;
import examination.teacherAndStudents.dto.StudyMaterialResponse;
import examination.teacherAndStudents.entity.StudyMaterial;
import org.springframework.data.domain.Page;

import java.util.List;


public interface StudyMaterialService {
    Page<StudyMaterialResponse> getAllMaterials(int page, int size, String sortBy, String sortDirection);
    void deleteMaterial(Long id);
    StudyMaterialResponse updateMaterial(Long id, StudyMaterialRequest request);
    StudyMaterialResponse getMaterialById(Long id);
    StudyMaterialResponse saveMaterial(StudyMaterialRequest request);



}

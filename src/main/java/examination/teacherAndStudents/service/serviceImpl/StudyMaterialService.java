package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.entity.StudyMaterial;
import examination.teacherAndStudents.repository.StudyMaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


public interface StudyMaterialService {
    List<StudyMaterial> getAllMaterials();
    void deleteMaterial(Long id);
    StudyMaterial getMaterialById(Long id);
    void saveMaterial(StudyMaterial material);



}

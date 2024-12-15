package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.entity.StudyMaterial;
import examination.teacherAndStudents.repository.StudyMaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyMaterialServiceImpl implements StudyMaterialService{

    private final  StudyMaterialRepository studyMaterialRepository;

    public List<StudyMaterial> getAllMaterials() {
        return studyMaterialRepository.findAll();
    }

    public StudyMaterial getMaterialById(Long id) {
        return studyMaterialRepository.findById(id).orElse(null);
    }

    public void saveMaterial(StudyMaterial material) {
        studyMaterialRepository.save(material);
    }

    public void deleteMaterial(Long id) {
        studyMaterialRepository.deleteById(id);
    }

}

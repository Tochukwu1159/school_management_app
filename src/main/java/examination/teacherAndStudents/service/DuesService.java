package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.DuesRequest;
import examination.teacherAndStudents.entity.Dues;

import java.util.List;

public interface DuesService {
    List<Dues> getAllDues();
    Dues getDuesById(Long id);
    Dues createDues(DuesRequest dues);
    Dues updateDues(Long id, DuesRequest updatedDues);
    boolean deleteDues(Long id);
}

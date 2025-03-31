package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.ScratchCardDTO;

public interface ScratchCardAssignmentService {

    ScratchCardDTO buyScratch(Long sessionId, Long termId) throws Exception;
    ScratchCardDTO getStudentScratchCard(Long sessionId, Long termId);
}

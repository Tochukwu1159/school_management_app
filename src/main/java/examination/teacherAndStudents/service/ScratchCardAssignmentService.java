package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.ScratchCardDTO;
import examination.teacherAndStudents.dto.ScratchCardPurchaseRequest;

public interface ScratchCardAssignmentService {
    ScratchCardDTO buyScratch(ScratchCardPurchaseRequest request) throws Exception;
    ScratchCardDTO getStudentScratchCard(Long sessionId, Long termId);
}
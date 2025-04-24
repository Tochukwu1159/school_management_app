package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.ScratchCardDTO;
import examination.teacherAndStudents.dto.ScratchCardValidationRequest;
import examination.teacherAndStudents.dto.ScratchCardValidationResponse;
import org.springframework.data.domain.Page;

public interface ScratchCardService {
    Page<ScratchCardDTO> getGeneratedScratchCards(int page, int size, Long sessionId, Long termId);
    ScratchCardValidationResponse validateScratchCard(ScratchCardValidationRequest request);
}
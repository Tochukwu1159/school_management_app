package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.GenerateScratchCardsRequest;
import examination.teacherAndStudents.dto.ScratchCardDTO;
import examination.teacherAndStudents.dto.ScratchCardValidationRequest;
import examination.teacherAndStudents.dto.ScratchCardValidationResponse;
import examination.teacherAndStudents.entity.ScratchCard;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ScratchCardService {

    List<ScratchCardDTO> generateScratchCards(GenerateScratchCardsRequest generateScratchCardsRequest) throws Exception;
    ScratchCardValidationResponse validateScratchCard(ScratchCardValidationRequest request);

    Page<ScratchCardDTO> getGeneratedScratchCards(int page, int size, Long sessionId,Long termId);
}

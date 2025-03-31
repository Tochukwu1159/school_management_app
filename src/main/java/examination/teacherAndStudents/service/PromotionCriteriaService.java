package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.PromotionCriteriaRequest;
import examination.teacherAndStudents.dto.PromotionCriteriaResponse;

import java.util.List;

public interface PromotionCriteriaService {
    PromotionCriteriaResponse createPromotionCriteria(PromotionCriteriaRequest request);
    PromotionCriteriaResponse updatePromotionCriteria(Long id, PromotionCriteriaRequest request);
    void deletePromotionCriteria(Long id);
    PromotionCriteriaResponse getPromotionCriteriaById(Long id);
    List<PromotionCriteriaResponse> getAllPromotionCriteria();
    List<PromotionCriteriaResponse> getPromotionCriteriaByClassBlock(Long classBlockId);
}
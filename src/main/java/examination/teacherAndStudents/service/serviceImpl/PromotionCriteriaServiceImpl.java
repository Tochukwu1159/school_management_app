package examination.teacherAndStudents.service.serviceImpl;


import examination.teacherAndStudents.dto.PromotionCriteriaRequest;
import examination.teacherAndStudents.dto.PromotionCriteriaResponse;
import examination.teacherAndStudents.entity.AcademicSession;
import examination.teacherAndStudents.entity.ClassBlock;
import examination.teacherAndStudents.entity.PromotionCriteria;
import examination.teacherAndStudents.error_handler.EntityAlreadyExistException;
import examination.teacherAndStudents.error_handler.ResourceNotFoundException;
import examination.teacherAndStudents.repository.AcademicSessionRepository;
import examination.teacherAndStudents.repository.ClassBlockRepository;
import examination.teacherAndStudents.repository.PromotionCriteriaRepository;
import examination.teacherAndStudents.service.PromotionCriteriaService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PromotionCriteriaServiceImpl implements PromotionCriteriaService {

    private final PromotionCriteriaRepository promotionCriteriaRepository;
    private final ClassBlockRepository classBlockRepository;
    private final AcademicSessionRepository academicSessionRepository;
    private final ModelMapper modelMapper;

    @Override
    public PromotionCriteriaResponse createPromotionCriteria(PromotionCriteriaRequest request) {
        // Validate all entities exist
        ClassBlock classBlock = classBlockRepository.findById(request.getClassBlockId())
                .orElseThrow(() -> new ResourceNotFoundException("Class block not found with ID: " + request.getClassBlockId()));

        AcademicSession currentSession = academicSessionRepository.findById(request.getCurrentSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Current session not found with ID: " + request.getCurrentSessionId()));

        AcademicSession futureSession = academicSessionRepository.findById(request.getFutureSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Future session not found with ID: " + request.getFutureSessionId()));

        ClassBlock promotedClass = classBlockRepository.findById(request.getPromotedClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Promoted class not found with ID: " + request.getPromotedClassId()));

        ClassBlock demotedClass = classBlockRepository.findById(request.getDemotedClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Demoted class not found with ID: " + request.getDemotedClassId()));

        // Check if criteria already exists for this class
        if (promotionCriteriaRepository.existsByClassBlockId(request.getClassBlockId())) {
            throw new EntityAlreadyExistException("Promotion criteria already exists for this class");
        }

        // Create and save new criteria
        PromotionCriteria criteria = PromotionCriteria.builder()
                .classBlock(classBlock)
                .currentSession(currentSession)
                .futureSession(futureSession)
                .promotedClass(promotedClass)
                .demotedClassId(demotedClass)
                .cutOffScore(request.getCutOffScore())
                .build();

        PromotionCriteria savedCriteria = promotionCriteriaRepository.save(criteria);
        return mapToResponse(savedCriteria);
    }

    @Override
    public PromotionCriteriaResponse updatePromotionCriteria(Long id, PromotionCriteriaRequest request) {
        PromotionCriteria criteria = promotionCriteriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion criteria not found with ID: " + id));

        // Only update fields that are allowed to change
        criteria.setCutOffScore(request.getCutOffScore());

        if (!criteria.getPromotedClass().getId().equals(request.getPromotedClassId())) {
            ClassBlock promotedClass = classBlockRepository.findById(request.getPromotedClassId())
                    .orElseThrow(() -> new ResourceNotFoundException("Promoted class not found"));
            criteria.setPromotedClass(promotedClass);
        }

        if (!criteria.getDemotedClassId().getId().equals(request.getDemotedClassId())) {
            ClassBlock demotedClass = classBlockRepository.findById(request.getDemotedClassId())
                    .orElseThrow(() -> new ResourceNotFoundException("Demoted class not found"));
            criteria.setDemotedClassId(demotedClass);
        }

        PromotionCriteria updatedCriteria = promotionCriteriaRepository.save(criteria);
        return mapToResponse(updatedCriteria);
    }

    @Override
    public void deletePromotionCriteria(Long id) {
        if (!promotionCriteriaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Promotion criteria not found with ID: " + id);
        }
        promotionCriteriaRepository.deleteById(id);
    }

    @Override
    public PromotionCriteriaResponse getPromotionCriteriaById(Long id) {
        PromotionCriteria criteria = promotionCriteriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion criteria not found with ID: " + id));
        return mapToResponse(criteria);
    }

    @Override
    public List<PromotionCriteriaResponse> getAllPromotionCriteria() {
        return promotionCriteriaRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PromotionCriteriaResponse> getPromotionCriteriaByClassBlock(Long classBlockId) {
        return promotionCriteriaRepository.findByClassBlockId(classBlockId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PromotionCriteriaResponse mapToResponse(PromotionCriteria criteria) {
        PromotionCriteriaResponse response = modelMapper.map(criteria, PromotionCriteriaResponse.class);

        // Map additional fields
        response.setClassBlockId(criteria.getClassBlock().getId());
        response.setClassBlockName(criteria.getClassBlock().getCurrentStudentClassName());
        response.setCurrentSessionId(criteria.getCurrentSession().getId());
        response.setCurrentSessionName(criteria.getCurrentSession().getName());
        response.setFutureSessionId(criteria.getFutureSession().getId());
        response.setFutureSessionName(criteria.getFutureSession().getName());
        response.setPromotedClassId(criteria.getPromotedClass().getId());
        response.setPromotedClassName(criteria.getPromotedClass().getCurrentStudentClassName());
        response.setDemotedClassId(criteria.getDemotedClassId().getId());
        response.setDemotedClassName(criteria.getDemotedClassId().getCurrentStudentClassName());

        return response;
    }
}
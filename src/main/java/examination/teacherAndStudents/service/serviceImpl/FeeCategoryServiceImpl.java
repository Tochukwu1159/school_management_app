package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.entity.FeeCategory;
import examination.teacherAndStudents.error_handler.BadRequestException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.FeeCategoryRepository;
import examination.teacherAndStudents.repository.FeeRepository;
import examination.teacherAndStudents.service.FeeCategoryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of FeeCategoryService for managing fee categories.
 */
@Service
@RequiredArgsConstructor
public class FeeCategoryServiceImpl implements FeeCategoryService {

    private static final Logger logger = LoggerFactory.getLogger(FeeCategoryServiceImpl.class);
    private final FeeCategoryRepository feeCategoryRepository;
    private final FeeRepository feeRepository;

    @Override
    @Transactional
    public FeeCategory createFeeCategory(String name) {
        validateCategoryName(name);

        if (feeCategoryRepository.existsByName(name)) {
            throw new BadRequestException("Fee category with name '" + name + "' already exists.");
        }

        FeeCategory category = FeeCategory.builder()
                .name(name)
                .build();

        FeeCategory savedCategory = feeCategoryRepository.save(category);
        logger.info("Created fee category ID {} with name {}", savedCategory.getId(), name);
        return savedCategory;
    }

    @Override
    @Transactional
    public FeeCategory updateFeeCategory(Long id, String name) {
        validateCategoryName(name);

        FeeCategory category = feeCategoryRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Fee category not found with ID: " + id));

        if (!name.equals(category.getName()) && feeCategoryRepository.existsByName(name)) {
            throw new BadRequestException("Fee category with name '" + name + "' already exists.");
        }

        category.setName(name);
        FeeCategory updatedCategory = feeCategoryRepository.save(category);
        logger.info("Updated fee category ID {} to name {}", id, name);
        return updatedCategory;
    }

    @Override
    @Transactional
    public void deleteFeeCategory(Long id) {
        FeeCategory category = feeCategoryRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Fee category not found with ID: " + id));

//         Check if category is used by any fees (optional, based on requirements)
         if (feeRepository.existsByCategoryId(id)) {
             throw new BadRequestException("Cannot delete fee category in use by fees.");
         }

        feeCategoryRepository.delete(category);
        logger.info("Deleted fee category ID {}", id);
    }

    @Override
    public FeeCategory getFeeCategoryById(Long id) {
        return feeCategoryRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Fee category not found with ID: " + id));
    }

    @Override
    public Page<FeeCategory> getAllFeeCategories(Pageable pageable) {
        return feeCategoryRepository.findAll(pageable);
    }

    @Override
    public List<FeeCategory> getAllFeeCategories() {
        return feeCategoryRepository.findAll();
    }

    private void validateCategoryName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Fee category name cannot be null or empty.");
        }
        if (name.length() > 100) {
            throw new BadRequestException("Fee category name cannot exceed 100 characters.");
        }
    }
}
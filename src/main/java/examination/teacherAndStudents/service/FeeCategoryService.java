package examination.teacherAndStudents.service;

import examination.teacherAndStudents.entity.FeeCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for managing fee categories.
 */
public interface FeeCategoryService {

    /**
     * Creates a new fee category.
     *
     * @param name The name of the fee category.
     * @return The created fee category.
     */
    FeeCategory createFeeCategory(String name);

    /**
     * Updates an existing fee category.
     *
     * @param id   The ID of the fee category.
     * @param name The updated name.
     * @return The updated fee category.
     */
    FeeCategory updateFeeCategory(Long id, String name);

    /**
     * Deletes a fee category.
     *
     * @param id The ID of the fee category.
     */
    void deleteFeeCategory(Long id);

    /**
     * Retrieves a fee category by ID.
     *
     * @param id The ID of the fee category.
     * @return The fee category.
     */
    FeeCategory getFeeCategoryById(Long id);

    /**
     * Retrieves all fee categories with pagination.
     *
     * @param pageable Pagination information.
     * @return A page of fee categories.
     */
    Page<FeeCategory> getAllFeeCategories(Pageable pageable);

    /**
     * Retrieves all fee categories as a list.
     *
     * @return List of all fee categories.
     */
    List<FeeCategory> getAllFeeCategories();
}
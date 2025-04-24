package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.FeeCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for FeeCategory entity.
 */
public interface FeeCategoryRepository extends JpaRepository<FeeCategory, Long> {

    /**
     * Checks if a fee category with the given name exists.
     *
     * @param name The category name.
     * @return True if the name exists, false otherwise.
     */
    boolean existsByName(String name);

    /**
     * Finds a fee category by name.
     *
     * @param name The category name.
     * @return Optional containing the fee category, if found.
     */
    Optional<FeeCategory> findByName(String name);
}
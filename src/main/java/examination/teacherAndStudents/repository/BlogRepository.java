package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Blog;
import examination.teacherAndStudents.entity.School;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {

    Optional<Blog> findByIdAndSchool(Long id, School school);

    @Query("SELECT b FROM Blog b WHERE " +
            "(:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:schoolId IS NULL OR b.school.id = :schoolId) AND " +
            "(:authorId IS NULL OR b.author.id = :authorId) AND " +
            "(:createdAtStart IS NULL OR b.createdAt >= :createdAtStart) AND " +
            "(:createdAtEnd IS NULL OR b.createdAt <= :createdAtEnd) AND " +
            "(:id IS NULL OR b.id = :id)")
    Page<Blog> findAllWithFilters(
            @Param("title") String title,
            @Param("schoolId") Long schoolId,
            @Param("authorId") Long authorId,
            @Param("createdAtStart") LocalDateTime createdAtStart,
            @Param("createdAtEnd") LocalDateTime createdAtEnd,
            @Param("id") Long id,
            Pageable pageable);

    Optional<Blog> findByIdAndSchoolId(Long blogId, Long id);
}

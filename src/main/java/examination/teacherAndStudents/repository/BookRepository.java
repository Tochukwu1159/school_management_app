package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Book;
import examination.teacherAndStudents.entity.School;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    @Query("SELECT b FROM Book b WHERE " +
            "b.school.id = :schoolId AND " +
            "(:id IS NULL OR b.id = :id) AND " +
            "(:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))) AND " +
            "(:shelfLocation IS NULL OR LOWER(b.shelfLocation) LIKE LOWER(CONCAT('%', :shelfLocation, '%'))) AND " +
            "(:createdAt IS NULL OR b.createdAt >= :createdAt)")
    Page<Book> findAllBySchoolWithFilters(
            @Param("schoolId") Long schoolId,
            @Param("id") Long id,
            @Param("title") String title,
            @Param("author") String author,
            @Param("shelfLocation") String shelfLocation,
            @Param("createdAt") LocalDateTime createdAt,
            Pageable pageable);

    boolean existsByTitleAndAuthorAndSchool(String title, String author, School school);
}

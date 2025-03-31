package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Visitors;
import examination.teacherAndStudents.utils.VisitorStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitorsRepository extends JpaRepository<Visitors, Long> {

    @Query("SELECT v FROM Visitors v WHERE " +
            "(:name IS NULL OR LOWER(v.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:phoneNumber IS NULL OR v.phoneNumber LIKE CONCAT('%', :phoneNumber, '%')) AND " +
            "(:email IS NULL OR LOWER(v.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
            "(:status IS NULL OR v.status = :status)")
    Page<Visitors> findAllWithFilters(
            @Param("name") String name,
            @Param("phoneNumber") String phoneNumber,
            @Param("email") String email,
            @Param("status") VisitorStatus status,
            Pageable pageable);

}

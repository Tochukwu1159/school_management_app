package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Complaint;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.utils.ComplainStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    Page<Complaint> findByComplainedBy(Profile userprofile, Pageable pageable);

    @Query("SELECT c FROM Complaint c JOIN c.complainedBy p JOIN p.user u WHERE "
            + "c.school.id = :schoolId AND "
            + "(:userName IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :userName, '%')) OR "
            + "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :userName, '%'))) AND "
            + "(:status IS NULL OR c.complainStatus = :status)")
    Page<Complaint> findByFilters(
            @Param("schoolId") Long schoolId,
            @Param("userName") String userName,
            @Param("status") ComplainStatus status,
            Pageable pageable
    );}

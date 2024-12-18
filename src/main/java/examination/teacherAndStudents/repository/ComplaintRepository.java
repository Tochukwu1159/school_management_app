package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Complaint;
import examination.teacherAndStudents.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    List<Complaint> findByComplainedBy(Profile userprofile);
}

package examination.teacherAndStudents.repository;


import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.utils.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DuesRepository extends JpaRepository<Dues, Long> {
    // You can add custom query methods if needed
}
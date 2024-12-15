package examination.teacherAndStudents.repository;


import examination.teacherAndStudents.entity.Dues;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.utils.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DuesRepository extends JpaRepository<Dues, Long> {
    // You can add custom query methods if needed
}
package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Cart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    List<Cart> findByProfileId(Long profileId);
}
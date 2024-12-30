package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.BookSaleAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface BookSaleAllocationRepository extends JpaRepository<BookSaleAllocation, Long> {
    List<BookSaleAllocation> findByProfileId(Long profileId);
}

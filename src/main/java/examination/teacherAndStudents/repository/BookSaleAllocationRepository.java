package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.BookSaleAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookSaleAllocationRepository extends JpaRepository<BookSaleAllocation, Long> {
    List<BookSaleAllocation> findByProfileId(Long profileId);
}

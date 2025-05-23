package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.BusRoute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusRouteRepository extends JpaRepository<BusRoute, Long> {
    @Query("SELECT r FROM BusRoute r LEFT JOIN FETCH r.stops WHERE r.id = :id")
    Optional<BusRoute> findByIdWithStops(@Param("id") Long id);

    Optional<BusRoute> findByIdAndSchoolId(Long routeId, Long id);

    Page<BusRoute> findBySchoolId(Long schoolId, Pageable paging);
}

package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface TransportTrackerRepository extends JpaRepository<TransportTracker, Long> {


    Optional<TransportTracker> findByBusAndSessionAndTerm(Bus transport, AcademicSession academicSession, StudentTerm term);

    void deleteByBus(Bus transport);

    List<TransportTracker> findByBusRouteAndSessionAndTermAndRemainingCapacityGreaterThan(BusRoute route, AcademicSession academicSession, StudentTerm term, int i);
}
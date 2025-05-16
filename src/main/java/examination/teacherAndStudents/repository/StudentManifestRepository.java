package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Bus;
import examination.teacherAndStudents.entity.BusRoute;
import examination.teacherAndStudents.entity.StudentManifest;
import examination.teacherAndStudents.utils.ManifestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentManifestRepository extends JpaRepository<StudentManifest, Long> {
    Page<StudentManifest> findByBus(Bus bus, Pageable pageable);

    List<StudentManifest> findByBus(Bus bus);

    @Query("SELECT CASE WHEN COUNT(sm) > 0 THEN true ELSE false END " +
            "FROM StudentManifest sm " +
            "WHERE sm.bus.busId = :busId AND sm.studentProfile.id = :profileId")
    boolean existsByBusIdAndStudentProfileId(@Param("busId") Long busId, @Param("profileId") Long profileId);

    @Query("SELECT sm FROM StudentManifest sm " +
            "WHERE sm.route = :route " +
            "AND (:academicSessionId IS NULL OR sm.academicSession.id = :academicSessionId) " +
            "AND (:studentTermId IS NULL OR sm.studentTerm.id = :studentTermId) " +
            "AND (:profileId IS NULL OR sm.studentProfile.id = :profileId) " +
            "AND (:status IS NULL OR sm.status = :status)")
    Page<StudentManifest> findByRouteAndFilters(
            @Param("route") BusRoute route,
            @Param("academicSessionId") Long academicSessionId,
            @Param("studentTermId") Long studentTermId,
            @Param("profileId") Long profileId,
            @Param("status") ManifestStatus status,
            Pageable pageable);

}
package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.MedicalRecord;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    @Query("SELECT mr FROM MedicalRecord mr WHERE " +
            "(:patientId IS NULL OR mr.patient.id = :patientId) AND " +
            "(:attendantId IS NULL OR mr.attendant.id = :attendantId) AND " +
            "(:id IS NULL OR mr.id = :id) AND " +
            "(:createdAt IS NULL OR mr.createdAt >= :createdAt)")
    Page<MedicalRecord> findAllWithFilters(
            @Param("patientId") Long patientId,
            @Param("attendantId") Long attendantId,
            @Param("id") Long id,
            @Param("createdAt") LocalDateTime createdAt,
            Pageable pageable);
}
package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.AuditLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLogEntry, Long> {
    List<AuditLogEntry> findByEntityTypeAndEntityId(String entityType, String entityId);

    List<AuditLogEntry> findByEntityTypeAndEntityIdAndTimestampBetween(
            String entityType, String entityId, LocalDateTime from, LocalDateTime to);
}
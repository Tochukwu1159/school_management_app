package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.MedicalRecordRequest;
import examination.teacherAndStudents.dto.MedicationDto;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public interface MedicalRecordService {
    MedicationDto addMedicalRecord(Long studentId, MedicalRecordRequest medicalRecordRequest);
    MedicationDto updateMedicalRecord(Long recordId, MedicalRecordRequest updatedRecordRequest);
    Page<MedicationDto> getAllMedicalRecords(
            Long patientId,
            Long attendantId,
            Long id,
            LocalDateTime createdAt,
            int page,
            int size,
            String sortBy,
            String sortDirection);
}

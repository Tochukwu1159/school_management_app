package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.MedicalRecordRequest;
import examination.teacherAndStudents.dto.MedicationDto;

import java.util.List;

public interface MedicalRecordService {
    MedicationDto addMedicalRecord(Long studentId, MedicalRecordRequest medicalRecordRequest);
    MedicationDto updateMedicalRecord(Long recordId, MedicalRecordRequest updatedRecordRequest);
    List<MedicationDto> getAllMedicalRecordsByStudent(Long studentId);
}

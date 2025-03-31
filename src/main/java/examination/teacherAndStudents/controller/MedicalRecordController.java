package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.MedicalRecordRequest;
import examination.teacherAndStudents.dto.MedicationDto;
import examination.teacherAndStudents.service.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/medical-records")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @Autowired
    public MedicalRecordController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

    @PostMapping("/create/{studentId}")
    public ResponseEntity<MedicationDto> createMedicalRecord(@PathVariable Long studentId, @RequestBody MedicalRecordRequest medicalRecordRequest) {
        MedicationDto createdRecord = medicalRecordService.addMedicalRecord(studentId,medicalRecordRequest);
        return new ResponseEntity<>(createdRecord, HttpStatus.CREATED);
    }
    @PutMapping("/update/{studentId}")
    public ResponseEntity<MedicationDto> updateMedicalRecord(@PathVariable Long studentId, @RequestBody MedicalRecordRequest medicalRecordRequest) {
        MedicationDto updatedRecord = medicalRecordService.updateMedicalRecord(studentId,medicalRecordRequest);
        return new ResponseEntity<>(updatedRecord, HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<Page<MedicationDto>> getAllMedicalRecords(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long attendantId,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Page<MedicationDto> records = medicalRecordService.getAllMedicalRecords(
                patientId,
                attendantId,
                id,
                createdAt,
                page,
                size,
                sortBy,
                sortDirection);

        return new ResponseEntity<>(records, HttpStatus.OK);
    }


    // Other methods for updating, retrieving, or deleting medical records can be added here
}
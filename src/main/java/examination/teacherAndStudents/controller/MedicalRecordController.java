package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.MedicalRecordRequest;
import examination.teacherAndStudents.dto.MedicationDto;
import examination.teacherAndStudents.service.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/medical-records")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @Autowired
    public MedicalRecordController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

    @PostMapping("/create")
    public ResponseEntity<MedicationDto> createMedicalRecord(@PathVariable Long studentId, @RequestBody MedicalRecordRequest medicalRecordRequest) {
        MedicationDto createdRecord = medicalRecordService.addMedicalRecord(studentId,medicalRecordRequest);
        return new ResponseEntity<>(createdRecord, HttpStatus.CREATED);
    }
    @PutMapping("/update")
    public ResponseEntity<MedicationDto> updateMedicalRecord(@PathVariable Long studentId, @RequestBody MedicalRecordRequest medicalRecordRequest) {
        MedicationDto updatedRecord = medicalRecordService.updateMedicalRecord(studentId,medicalRecordRequest);
        return new ResponseEntity<>(updatedRecord, HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<MedicationDto>> updateMedicalRecord(Long studentId) {
        List<MedicationDto> getAllMedicalRecordsByStudent = medicalRecordService.getAllMedicalRecordsByStudent(studentId);
        return new ResponseEntity<>(getAllMedicalRecordsByStudent, HttpStatus.OK);
    }



    // Other methods for updating, retrieving, or deleting medical records can be added here
}
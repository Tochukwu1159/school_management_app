package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.MedicalRecordRequest;
import examination.teacherAndStudents.dto.MedicationDto;
import examination.teacherAndStudents.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @PostMapping("/create/{studentId}")
    public ResponseEntity<ApiResponse<MedicationDto>> createMedicalRecord(@PathVariable Long studentId, @RequestBody MedicalRecordRequest medicalRecordRequest) {
        MedicationDto createdRecord = medicalRecordService.addMedicalRecord(studentId, medicalRecordRequest);
        ApiResponse<MedicationDto> apiResponse = new ApiResponse<>("Medical record created successfully", true, createdRecord);
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PutMapping("/update/{studentId}")
    public ResponseEntity<ApiResponse<MedicationDto>> updateMedicalRecord(@PathVariable Long studentId, @RequestBody MedicalRecordRequest medicalRecordRequest) {
        MedicationDto updatedRecord = medicalRecordService.updateMedicalRecord(studentId, medicalRecordRequest);
        ApiResponse<MedicationDto> apiResponse = new ApiResponse<>("Medical record updated successfully", true, updatedRecord);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/all")
    public ResponseEntity<Page<ApiResponse<MedicationDto>>> getAllMedicalRecords(
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

        Page<ApiResponse<MedicationDto>> apiResponsePage = new PageImpl<>(
                records.getContent().stream()
                        .map(record -> new ApiResponse<>("Medical record fetched successfully", true, record))
                        .collect(Collectors.toList()),
                records.getPageable(),
                records.getTotalElements()
        );

        return ResponseEntity.ok(apiResponsePage);
    }

    // Other methods for retrieving or deleting medical records can be added here
}

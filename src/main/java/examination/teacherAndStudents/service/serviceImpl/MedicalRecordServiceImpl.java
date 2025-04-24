package examination.teacherAndStudents.service.serviceImpl;


import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.MedicalRecordRequest;
import examination.teacherAndStudents.dto.MedicationDto;
import examination.teacherAndStudents.entity.MedicalRecord;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.MedicalRecordRepository;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl implements MedicalRecordService {


    private final MedicalRecordRepository medicalRecordRepository;


    private final UserRepository userRepository;

    private final ModelMapper modelMapper;
    private final ProfileRepository profileRepository;

    // Other dependencies and methods

    public MedicationDto addMedicalRecord(Long studentId, MedicalRecordRequest medicalRecordRequest) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User attendant = userRepository.findByEmail(email)
                    .orElseThrow(() -> new CustomNotFoundException("attendant  not found"));

            Profile attendantProfile = profileRepository.findByUser(attendant)
                    .orElseThrow(() -> new CustomNotFoundException("Attendant profile not found"));

            User user = userRepository.findById(studentId)
                    .orElseThrow(() -> new CustomNotFoundException("User  not found"));

            Profile profile = profileRepository.findByUser(user)
                    .orElseThrow(() -> new CustomNotFoundException("Profile not found"));



            MedicalRecord medicalRecord = new MedicalRecord();
            medicalRecord.setAttendant(attendantProfile);
            medicalRecord.setPatient(profile);
            medicalRecord.setDetails(medicalRecordRequest.getDetails());

            // Add any other fields as needed

             medicalRecordRepository.save(medicalRecord);
             return modelMapper.map(medicalRecord, MedicationDto.class);
        } catch (Exception e) {
            throw new CustomInternalServerException("Error adding medical record: " + e.getMessage());
        }
    }

    public MedicationDto updateMedicalRecord(Long recordId, MedicalRecordRequest updatedRecordRequest) {
        try {
            MedicalRecord existingRecord = medicalRecordRepository.findById(recordId)
                    .orElseThrow(() -> new CustomNotFoundException("Medical record not found"));

            // Update fields based on the updatedRecordRequest

            existingRecord.setDetails(updatedRecordRequest.getDetails());
            // Update any other fields as needed

             medicalRecordRepository.save(existingRecord);
           return modelMapper.map(existingRecord, MedicationDto.class);
        } catch (Exception e) {
            throw new CustomInternalServerException("Error updating medical record: " + e.getMessage());
        }
    }

    public Page<MedicationDto> getAllMedicalRecords(
            Long patientId,
            Long attendantId,
            Long id,
            LocalDateTime createdAt,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        try {
            // Create Pageable object
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            // Fetch filtered medical records
            Page<MedicalRecord> recordsPage = medicalRecordRepository.findAllWithFilters(
                    patientId,
                    attendantId,
                    id,
                    createdAt,
                    pageable);

            // Map to DTO
            return recordsPage.map(record -> modelMapper.map(record, MedicationDto.class));

        } catch (Exception e) {
            throw new CustomInternalServerException("Error retrieving medical records: " + e.getMessage());
        }
    }

    // Add other methods as needed for medical records management

}

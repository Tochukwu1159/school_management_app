package examination.teacherAndStudents.service.serviceImpl;


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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
            Optional<User> student = userRepository.findById(studentId);
            Optional<Profile> studentProfile = profileRepository.findByUser(student.get());

            MedicalRecord medicalRecord = new MedicalRecord();
            medicalRecord.setUser(studentProfile.get());
            medicalRecord.setRecordDate(LocalDateTime.now());
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

    public List<MedicationDto> getAllMedicalRecordsByStudent(Long studentId) {
        try {
            Optional<User> student = userRepository.findById(studentId);
            List<MedicalRecord> medicationDtoList = medicalRecordRepository.findAllByUser(student);
            return medicationDtoList.stream().map((element) -> modelMapper.map(element, MedicationDto.class)).collect(Collectors.toList());
        } catch (Exception e) {
            throw new CustomInternalServerException("Error retrieving medical records: " + e.getMessage());
        }
    }

    // Add other methods as needed for medical records management

}

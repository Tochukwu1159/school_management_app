package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.StaffRequest;
import examination.teacherAndStudents.dto.StaffResponse;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.EntityNotFoundException;
import examination.teacherAndStudents.objectMapper.StaffMapper;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.StaffService;
import examination.teacherAndStudents.utils.AccountUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StaffServiceImpl implements StaffService {

    private final UserRepository userRepository;

    private final StaffMapper staffMapper;
    private final ProfileRepository profileRepository;

    // Create (Add)  staff
    public StaffResponse createStaff(StaffRequest staffRequest) {
        try {
          mapToStaff(staffRequest);
          mapToProfile(staffRequest);
            return mapToStaffResponse(staffRequest);
        } catch (Exception e) {
            throw new CustomInternalServerException("Error creating  staff: " + e.getMessage());
        }
    }

    // Update  staff
    public StaffResponse updateStaff(Long StaffId, StaffRequest updatedStaff) {
        try {
            User existingStaff = userRepository.findById(StaffId)
                    .orElseThrow(() -> new CustomNotFoundException(" staff not found with ID: " + StaffId));
            existingStaff.setFirstName(updatedStaff.getFirstName());
            existingStaff.setLastName(updatedStaff.getLastName());
            existingStaff.setMiddleName(updatedStaff.getMiddleName());
            userRepository.save(existingStaff);
            mapToProfile(updatedStaff);
            return mapToStaffResponse(updatedStaff);
        } catch (Exception e) {
            throw new CustomInternalServerException("Error updating  staff: " + e.getMessage());
        }
    }

    // Get all  staff
    public Page<StaffResponse> findAllStaff(String filter, int page, int size, String sortBy) {
        Pageable paging = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        Page<User> staffPage = userRepository.findAllByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCaseOrId(filter, filter, Long.valueOf(filter), paging);
        return staffPage.map(staffMapper::mapToStaffResponse);
    }


    // Get  staff by ID
    public StaffResponse findStaffById(Long StaffId) {
        User staff = userRepository.findById(StaffId)
                .orElseThrow(() -> new CustomNotFoundException(" staff not found with ID: " + StaffId));
        return staffMapper.mapToStaffResponse(staff);
    }

    // Delete  staff
    public void deleteStaff(Long StaffId) {
        try {
            userRepository.deleteById(StaffId);
        } catch (Exception e) {
            // Handle any exceptions
            throw new CustomInternalServerException("Error deleting  staff: " + e.getMessage());
        }
    }


    private  User mapToStaff(StaffRequest staffRequest){
        User staff = new User();
    staff.setFirstName(staffRequest.getFirstName());
    staff.setLastName(staffRequest.getLastName());
    staff.setRoles(staffRequest.getRoles());
    staff.setMiddleName(staffRequest.getMiddleName());
    staff.setEmail(staff.getEmail());
    userRepository.save(staff);
    return staff;
}

    private  Profile mapToProfile(StaffRequest staffRequest){
        Profile staffProfile = new Profile();
        staffProfile.setAddress(staffRequest.getAddress());
        staffProfile.setGender(staffProfile.getGender());
        staffProfile.setAcademicQualification(staffProfile.getAcademicQualification());
        staffProfile.setUniqueRegistrationNumber(AccountUtils.generateStaffId());
        staffProfile.setReligion(staffRequest.getReligion());
        staffProfile.setPhoneNumber(staffRequest.getPhoneNumber());
        staffProfile.setDateOfBirth(staffRequest.getDateOfBirth());
        staffProfile.setAdmissionDate(staffRequest.getAdmissionDate());
        profileRepository.save(staffProfile);
        return staffProfile;
    }

    private  StaffResponse mapToStaffResponse(StaffRequest request){
        StaffResponse staffResponse = new StaffResponse();
        staffResponse.setAge(request.getAge());
        staffResponse.setAddress(request.getAddress());
        staffResponse.setGender(request.getGender());
        staffResponse.setBankName(request.getBankName());
        staffResponse.setDateOfBirth(request.getDateOfBirth());
        staffResponse.setResume(request.getResume());
        staffResponse.setReligion(request.getReligion());
        staffResponse.setFirstName(request.getFirstName());
        staffResponse.setFirstName(request.getFirstName());
        staffResponse.setAcademicQualification(request.getAcademicQualification());
        staffResponse.setUniqueRegistrationNumber(request.getUniqueRegistrationNumber());
        staffResponse.setPhoneNumber(request.getPhoneNumber());
        staffResponse.setContractType(request.getContractType());
        staffResponse.setBankAccountNumber(request.getBankAccountNumber());
        return staffResponse;
    }

}

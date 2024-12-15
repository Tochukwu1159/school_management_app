package examination.teacherAndStudents.objectMapper;

import examination.teacherAndStudents.dto.StaffRequest;
import examination.teacherAndStudents.dto.StaffResponse;
import examination.teacherAndStudents.entity.User;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StaffMapper {
    private final ModelMapper modelMapper;

    public User mapToStaff(StaffRequest staffRequest) {
        return  modelMapper.map(staffRequest, User.class);
    }

    public StaffResponse mapToStaffResponse(User staff) {
        return modelMapper.map(staff, StaffResponse.class);

    }
}


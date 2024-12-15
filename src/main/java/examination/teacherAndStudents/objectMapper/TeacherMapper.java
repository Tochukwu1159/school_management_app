package examination.teacherAndStudents.objectMapper;

import examination.teacherAndStudents.dto.TeacherRequest;
import examination.teacherAndStudents.dto.TeacherResponse;
import examination.teacherAndStudents.entity.User;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TeacherMapper {
    private final ModelMapper modelMapper;

    public User mapToTeacher(TeacherRequest teacherRequest) {
    return  modelMapper.map(teacherRequest, User.class);
           }

    public TeacherResponse mapToTeacherResponse(User teacher) {
    return modelMapper.map(teacher, TeacherResponse.class );
    }
}

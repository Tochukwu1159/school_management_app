package examination.teacherAndStudents.objectMapper;

import examination.teacherAndStudents.dto.SubjectRequest;
import examination.teacherAndStudents.dto.SubjectResponse;
import examination.teacherAndStudents.entity.Subject;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubjectMapper {
    private final ModelMapper modelMapper;
    public Subject mapToSubject(SubjectRequest subjectRequest) {
        return modelMapper.map(subjectRequest, Subject.class);
    }

    public SubjectResponse mapToSubjectResponse(Subject subject) {
        return modelMapper.map(subject, SubjectResponse.class);
    }
}

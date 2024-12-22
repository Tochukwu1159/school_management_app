package examination.teacherAndStudents.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LibraryMemberResponse {
    private Long id;
    private String userRole;
    private String userClass;
    private String userName;
    private String memberId;


    // Constructors, getters, and setters
}
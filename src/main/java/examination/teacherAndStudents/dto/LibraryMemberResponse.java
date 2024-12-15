package examination.teacherAndStudents.dto;

import lombok.Data;

@Data
public class LibraryMemberResponse {
    private Long id;
    private String userRole;
    private String userClass;
    private String userName;
    private String memberId;


    // Constructors, getters, and setters
}
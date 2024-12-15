package examination.teacherAndStudents.dto;

import lombok.Data;

@Data
public class LibraryMembershipRequest {
        private String userRole;
        private Long userClassId;
        private String userUniqueRegistrationNumber;

        private String memberId;


}

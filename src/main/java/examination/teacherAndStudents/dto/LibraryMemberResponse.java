package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.MembershipStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LibraryMemberResponse {

    private Long id;
    private String memberId;
    private String studentUniqueRegistrationNumber;
    private String studentName;
    private MembershipStatus status;
    private LocalDateTime joinDate;
    private LocalDateTime expiryDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
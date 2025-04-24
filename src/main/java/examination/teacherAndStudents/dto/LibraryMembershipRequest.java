package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.MembershipStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LibraryMembershipRequest {

        @NotNull
        private String userUniqueRegistrationNumber;

        private Long userClassId; // Optional, for validation if needed

        private MembershipStatus status; // Optional, for updates

        private LocalDateTime expiryDate; // Optional, for custom expiry
}
package examination.teacherAndStudents.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor // Required for Jackson deserialization
@AllArgsConstructor // Optional, for convenience
public class StudentManifestRequest {

    @NotNull(message = "Manifest entry cannot be null")
    private ManifestEntry manifestEntries;

    @Data
    @Builder
    @NoArgsConstructor // Required for Jackson deserialization
    @AllArgsConstructor // Optional, for convenience
    public static class ManifestEntry {
        @NotNull(message = "Profile ID is required")
        private Long profileId;

        @NotNull(message = "Status is required")
        private String status;

        private String pickupPerson;

        private String notes;

    }
}
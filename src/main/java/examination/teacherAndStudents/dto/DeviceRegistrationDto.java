package examination.teacherAndStudents.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeviceRegistrationDto(
        @NotNull Long userId,
        @NotBlank String deviceId,
        @NotBlank String fcmToken
) {}


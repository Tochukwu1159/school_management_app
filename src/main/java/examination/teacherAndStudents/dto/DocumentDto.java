package examination.teacherAndStudents.dto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public  class DocumentDto {
    @NotNull(message = "Document type is required")
    private String documentType;

    @NotNull(message = "Document file is required")
    private MultipartFile file;
}

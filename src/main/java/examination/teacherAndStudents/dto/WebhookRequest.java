package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
@Data
@AllArgsConstructor
@Builder
public class WebhookRequest {
    private  String payload;
    private   String signature;
    private  String provider;
}
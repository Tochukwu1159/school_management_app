
package examination.teacherAndStudents.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Data
@NoArgsConstructor
public class SchoolLoginResponse {
    @Getter
    private String token;
    private SchoolResponse schoolResponse;

    public SchoolLoginResponse(String token, SchoolResponse schoolResponse) {
        this.token = token;
        this.schoolResponse = schoolResponse;
    }

}
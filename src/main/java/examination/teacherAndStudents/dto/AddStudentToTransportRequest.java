package examination.teacherAndStudents.dto;

import lombok.Data;

@Data
public class AddStudentToTransportRequest {
    private Long transportTrackerId;
    private Long transportId;

    private Long transportAllocationId;
}


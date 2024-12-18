 package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.AttendanceStatus;
import examination.teacherAndStudents.utils.StudentTerm;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

 @Data
    public class AttendanceRequest {
        private LocalDate date;
        private Long studentTermId;
        private AttendanceStatus status;
        private Long studentId;

}

package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.entity.Profile;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TeacherAttendanceResponse {
    private ProfileData teacherProfile;
    private double attendancePercentage;

    public TeacherAttendanceResponse(ProfileData teacherProfile, double attendancePercentage) {
        this.teacherProfile = teacherProfile;
        this.attendancePercentage = attendancePercentage;
    }

}

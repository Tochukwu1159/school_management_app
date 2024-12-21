package examination.teacherAndStudents.dto;

public class StudentAttendanceResponse {
    private ProfileData studentProfileData;
    private double attendancePercentage;

    public StudentAttendanceResponse(ProfileData studentProfileData, double attendancePercentage) {
        this.studentProfileData = studentProfileData;
        this.attendancePercentage = attendancePercentage;
    }

    // Getters and Setters

    public ProfileData getStudentProfileData() {
        return studentProfileData;
    }

    public void setStudentProfileData(ProfileData studentProfileData) {
        this.studentProfileData = studentProfileData;
    }

    public double getAttendancePercentage() {
        return attendancePercentage;
    }

    public void setAttendancePercentage(double attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
    }
}

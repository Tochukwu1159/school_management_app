package examination.teacherAndStudents.dto;

public class StudentAttendanceResponse {
    private ProfileData studentProfileData;
    private double attendancePercentage;
    private long daysPresent;
    private long daysAbsent;
    private long totalDays;

    public StudentAttendanceResponse(ProfileData studentProfileData, double attendancePercentage, long daysPresent, long daysAbsent, long totalDays) {
        this.studentProfileData = studentProfileData;
        this.attendancePercentage = attendancePercentage;
        this.daysPresent = daysPresent;
        this.daysAbsent = daysAbsent;
        this.totalDays = totalDays;
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

    public long getDaysPresent() {
        return daysPresent;
    }

    public void setDaysPresent(long daysPresent) {
        this.daysPresent = daysPresent;
    }

    public long getDaysAbsent() {
        return daysAbsent;
    }

    public void setDaysAbsent(long daysAbsent) {
        this.daysAbsent = daysAbsent;
    }

    public long getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(long totalDays) {
        this.totalDays = totalDays;
    }
}
package examination.teacherAndStudents.dto;

public class AddStudentToTransportRequest {
    private Long transportTrackerId;
    private Long transportId;
    private Long studentId;
    private Long academicYearId;
    private Long termId;

    // Getters and Setters
    public Long getTransportTrackerId() {
        return transportTrackerId;
    }

    public void setTransportTrackerId(Long transportTrackerId) {
        this.transportTrackerId = transportTrackerId;
    }

    public Long getTransportId() {
        return transportId;
    }

    public void setTransportId(Long transportId) {
        this.transportId = transportId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getAcademicYearId() {
        return academicYearId;
    }

    public void setAcademicYearId(Long academicYearId) {
        this.academicYearId = academicYearId;
    }

    public Long getTermId() {
        return termId;
    }

    public void setTermId(Long termId) {
        this.termId = termId;
    }
}


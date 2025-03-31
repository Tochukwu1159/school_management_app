package examination.teacherAndStudents.utils;

public enum SubmissionStatus {
    ASSIGNED,   // Teacher assigned the homework
    SUBMITTED,
    LATE,// At least one student submitted it
    GRADED,     // Teacher graded the submissions
    CLOSED      // Submission period is over
}

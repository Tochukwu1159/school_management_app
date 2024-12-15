package examination.teacherAndStudents.error_handler;

public class AttendanceAlreadyTakenException extends RuntimeException {

    public AttendanceAlreadyTakenException(String message) {
        super(message);
    }
}


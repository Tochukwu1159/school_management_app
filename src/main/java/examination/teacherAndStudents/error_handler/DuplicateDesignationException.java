package examination.teacherAndStudents.error_handler;
public class DuplicateDesignationException extends RuntimeException {
    public DuplicateDesignationException(String message) {
        super(message);
    }
}
package examination.teacherAndStudents.error_handler;



public class UserServiceException extends RuntimeException {
    public UserServiceException(String message) {
        super(message);
    }
}

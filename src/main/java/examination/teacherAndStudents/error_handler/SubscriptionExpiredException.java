package examination.teacherAndStudents.error_handler;

public class SubscriptionExpiredException extends RuntimeException{
    public SubscriptionExpiredException(String message) {
        super(message);
    }
}
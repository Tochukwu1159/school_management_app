package examination.teacherAndStudents.error_handler;


public class EventAlreadyExpired extends RuntimeException {
    public EventAlreadyExpired(String message) {
        super(message);
    }

}
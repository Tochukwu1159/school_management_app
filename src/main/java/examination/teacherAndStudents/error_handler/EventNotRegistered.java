package examination.teacherAndStudents.error_handler;

public class EventNotRegistered extends RuntimeException{
    public EventNotRegistered(String message) {
        super(message);
    }
}
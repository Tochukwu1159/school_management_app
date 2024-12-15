package examination.teacherAndStudents.error_handler;

public class CustomUserAlreadyRegistered extends RuntimeException {
    public CustomUserAlreadyRegistered(String message) {
        super(message);
        System.out.println(message+ "message");
    }
}

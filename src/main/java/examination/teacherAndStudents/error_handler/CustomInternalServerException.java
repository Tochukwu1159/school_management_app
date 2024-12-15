package examination.teacherAndStudents.error_handler;

public class CustomInternalServerException extends RuntimeException {
    public CustomInternalServerException(String message) {
        super(message);
        System.out.println(message+ "messsage");
    }
}
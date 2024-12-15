package examination.teacherAndStudents.error_handler;

public class CustomNotFoundException extends RuntimeException {
    public CustomNotFoundException(String message) {
        super(message);
        System.out.println(message+ "messsage");
    }
}



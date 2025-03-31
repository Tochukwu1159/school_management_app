package examination.teacherAndStudents.error_handler;

import com.google.gson.JsonSyntaxException;



public class PaymentProcessingException extends RuntimeException{
    public PaymentProcessingException(String message) {
        super(message);
    }
}
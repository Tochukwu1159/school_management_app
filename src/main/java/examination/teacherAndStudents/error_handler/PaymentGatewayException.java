package examination.teacherAndStudents.error_handler;

public class PaymentGatewayException extends RuntimeException{
    public PaymentGatewayException(String message) {
        super(message);
    }
}
package examination.teacherAndStudents.error_handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        return new ResponseEntity<>(new ApiResponse<>("Validation errors", false, errors), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<?> AuthenticationFailedException(AuthenticationFailedException ex) {
        return new ResponseEntity<>(new ApiResponse<>(ex.getMessage(), false), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(SubscriptionExpiredException.class)
    public ResponseEntity<?> SubscriptionExpiredException(SubscriptionExpiredException ex) {
        return new ResponseEntity<>(new ApiResponse<>(ex.getMessage(), false), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(CustomInternalServerException.class)
    public ResponseEntity<?> CustomInternalServerException(CustomInternalServerException ex) {
        return new ResponseEntity<>(new ApiResponse<>(ex.getMessage(), false), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(CustomNotFoundException.class)
    public ResponseEntity<?> ResourceNotFoundException(CustomNotFoundException ex) {
        return new ResponseEntity<>(new ApiResponse<>(ex.getMessage(), false), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AttendanceAlreadyTakenException.class)
    public ResponseEntity<?> AttendanceAlreadyTakenException(AttendanceAlreadyTakenException ex) {
        return new ResponseEntity<>(new ApiResponse<>(ex.getMessage(), false), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<?> EventNotFoundException(EventNotFoundException ex) {
        return new ResponseEntity<>(new ApiResponse<>(ex.getMessage(), false), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<?> InsufficientBalanceException(InsufficientBalanceException ex) {
        return new ResponseEntity<>(new ApiResponse<>(ex.getMessage(), false), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserServiceException.class)
    public ResponseEntity<?> UserServiceException(UserServiceException ex) {
        return new ResponseEntity<>(new ApiResponse<>(ex.getMessage(), false), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(EventNotRegistered.class)
    public ResponseEntity<?> EventNotRegistered(EventNotRegistered ex) {
        return new ResponseEntity<>(new ApiResponse<>(ex.getMessage(), false), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CustomUserAlreadyRegistered.class)
    public ResponseEntity<?> CustomUserAlreadyRegistered(CustomUserAlreadyRegistered ex) {
        return new ResponseEntity<>(new ApiResponse<>(ex.getMessage(), false), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(EventAlreadyExpired.class)
    public ResponseEntity<?> EventAlreadyExpired(EventAlreadyExpired ex) {
        return new ResponseEntity<>(new ApiResponse<>(ex.getMessage(), false), HttpStatus.GONE);
    }

    @ExceptionHandler(UploadFailedException.class)
    public ResponseEntity<?> UploadFailedException(UploadFailedException ex) {
        return new ResponseEntity<>(new ApiResponse<>(ex.getMessage(), false), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(EmailSendingException.class)
    public ResponseEntity<?> EmailSendingException(EmailSendingException ex) {
        return new ResponseEntity<>(new ApiResponse<>(ex.getMessage(), false), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<?> UserAlreadyExistException(UserAlreadyExistException ex) {
        return new ResponseEntity<>(new ApiResponse<>(ex.getMessage(), false), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<?> TokenExpiredException(TokenExpiredException ex) {
        return new ResponseEntity<>(new ApiResponse<>(ex.getMessage(), false), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DuplicateDesignationException.class)
    public ResponseEntity<?> DuplicateDesignationException(DuplicateDesignationException ex) {
        return new ResponseEntity<>(new ApiResponse<>(ex.getMessage(), false), HttpStatus.CONFLICT);
    }


    @ExceptionHandler(ScratchCardException.class)
    public ResponseEntity<?> ScratchCardException(ScratchCardException ex) {
        return new ResponseEntity<>(new ApiResponse<>(ex.getMessage(), false), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<?> UsernameNotFoundException(UsernameNotFoundException ex) {
        return new ResponseEntity<>(new ApiResponse<>(ex.getMessage(), false), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> BadRequestException(BadRequestException ex) {
        return new ResponseEntity<>(new ApiResponse<>(ex.getMessage(), false), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserPasswordMismatchException.class)
    public ResponseEntity<?> UserPasswordMismatchException(UserPasswordMismatchException ex) {
        return new ResponseEntity<>(new ApiResponse<>(ex.getMessage(), false), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityAlreadyExistException.class)
    public ResponseEntity<?> EntityAlreadyExistException(EntityAlreadyExistException ex) {
        return new ResponseEntity<>(new ApiResponse<>(ex.getMessage(), false), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> IllegalJwtExceptionHandler(IllegalArgumentException ex) {
        return new ResponseEntity<>(new ApiResponse<>(ex.getMessage(), false), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> ResourceNotFoundException(ResourceNotFoundException ex) {
        return new ResponseEntity<>(new ApiResponse<>(ex.getMessage(), false), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<?> IOException(IOException ex) {
        return new ResponseEntity<>(new ApiResponse<>(ex.getMessage(), false), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotVerifiedException.class)
    public ResponseEntity<?> UserNotVerifiedException(UserNotVerifiedException ex) {
        return new ResponseEntity<>(new ApiResponse<>(ex.getMessage(), false), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<?> WorkshopNotFoundException(UnauthorizedException ex) {
        return new ResponseEntity<>(new ApiResponse<>(ex.getMessage(), false), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<?> PaymentProcessingException(PaymentProcessingException ex) {
        return new ResponseEntity<>(new ApiResponse<>(ex.getMessage(), false), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> EntityNotFoundException(EntityNotFoundException ex) {
        return new ResponseEntity<>(new ApiResponse<>(ex.getMessage(), false), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> NotFoundException(NotFoundException ex) {
        return new ResponseEntity<>(new ApiResponse<>(ex.getMessage(), false), HttpStatus.NOT_FOUND);
    }
}

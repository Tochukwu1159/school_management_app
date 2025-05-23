package examination.teacherAndStudents.error_handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ExceptionController {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionController.class);


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        return new ResponseEntity<>(new ApiResponse<>("Validation errors", false, errors), HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        String supportedMethods = ex.getSupportedHttpMethods() != null
                ? ex.getSupportedHttpMethods().stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "))
                : "None";
        String message = String.format(
                "Method %s not supported for %s. Supported methods: %s",
                ex.getMethod(),
                request.getRequestURI(),
                supportedMethods
        );
        logger.warn("Invalid HTTP method: {}", message);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message(message)
                .status(false)
                .timeCreated(LocalDateTime.now())
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }



    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        String message = String.format(
                "Access denied for %s: Insufficient permissions",
                request.getRequestURI()
        );
        logger.warn("Unauthorized access attempt: {}", message);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message(message)
                .status(false)
                .timeCreated(LocalDateTime.now())
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(JwtTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleJwtTokenException(
            JwtTokenException ex, HttpServletRequest request) {
        String message = String.format(
                "Invalid or expired token for %s: %s. Please refresh your token or log in again.",
                request.getRequestURI(),
                ex.getMessage()
        );
        logger.warn("Token validation failed: {}", message);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message(message)
                .status(false)
                .timeCreated(LocalDateTime.now())
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }



    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleExpiredJwtException(
            ExpiredJwtException ex, HttpServletRequest request) {
        String message = String.format(
                "Token expired for %s: %s",
                request.getRequestURI(),
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.<Void>builder()
                        .message(message)
                        .status(false)
                        .timeCreated(LocalDateTime.now())
                        .data(null)
                        .build());
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleJwtException(
            JwtException ex, HttpServletRequest request) {
        String message = String.format(
                "Invalid or expired token for %s: %s",
                request.getRequestURI(),
                ex.getMessage()
        );
        logger.warn("Token validation failed: {}", message);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message(message)
                .status(false)
                .timeCreated(LocalDateTime.now())
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }



    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<?> AuthenticationFailedException(AuthenticationFailedException ex) {
        return new ResponseEntity<>(new ApiResponse<>(ex.getMessage(), false), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(PaymentGatewayException.class)
    public ResponseEntity<?> PaymentGatewayException(PaymentGatewayException ex) {
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

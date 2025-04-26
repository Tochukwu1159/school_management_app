package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApplicationReviewDto;
import examination.teacherAndStudents.dto.PaymentProviderRequest;
import examination.teacherAndStudents.dto.PaymentResponse;
import examination.teacherAndStudents.dto.UserResponse;
import examination.teacherAndStudents.error_handler.BadRequestException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.PaymentProcessingException;
import examination.teacherAndStudents.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admissions")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    /**
     * Endpoint to initiate payment for an application fee.
     */
    @PostMapping("/applications/{applicationId}/pay")
    public ResponseEntity<PaymentResponse> payApplicationFee(
            @PathVariable Long applicationId,
            @Valid @RequestBody PaymentProviderRequest paymentRequest) {
        try {
            PaymentResponse response = applicationService.payApplicationFee(applicationId, paymentRequest);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (CustomNotFoundException e) {
            return new ResponseEntity<>(
                    PaymentResponse.builder()
                            .reference(null)
                            .authorizationUrl(null)
                            .amount(null)
                            .build(), // Minimal response for error
                    HttpStatus.NOT_FOUND
            );
        } catch (IllegalStateException | PaymentProcessingException e) {
            return new ResponseEntity<>(
                    PaymentResponse.builder()
                            .reference(null)
                            .authorizationUrl(null)
                            .amount(null)
                            .build(),
                    HttpStatus.BAD_REQUEST
            );
        } catch (RuntimeException e) {
            return new ResponseEntity<>(
                    PaymentResponse.builder()
                            .reference(null)
                            .authorizationUrl(null)
                            .amount(null)
                            .build(),
                    HttpStatus.FORBIDDEN
            );
        }
    }

    /**
     * Endpoint to review an admission application.
     */
    @PutMapping("/applications/{applicationId}/review")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> reviewApplication(
            @PathVariable Long applicationId,
            @Valid @RequestBody ApplicationReviewDto review) {
        try {
            UserResponse response = applicationService.reviewApplication(applicationId, review);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (CustomNotFoundException e) {
            return new ResponseEntity<>(
                    UserResponse.builder()
                            .responseCode("404")
                            .responseMessage(e.getMessage())
                            .build(),
                    HttpStatus.NOT_FOUND
            );
        } catch (BadRequestException e) {
            return new ResponseEntity<>(
                    UserResponse.builder()
                            .responseCode("400")
                            .responseMessage(e.getMessage())
                            .build(),
                    HttpStatus.BAD_REQUEST
            );
        } catch (RuntimeException e) {
            return new ResponseEntity<>(
                    UserResponse.builder()
                            .responseCode("403")
                            .responseMessage(e.getMessage())
                            .build(),
                    HttpStatus.FORBIDDEN
            );
        }
    }
}
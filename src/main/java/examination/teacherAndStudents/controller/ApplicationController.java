package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.*;
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
    public ResponseEntity<ApiResponse<PaymentResponse>> payApplicationFee(
            @PathVariable Long applicationId,
            @Valid @RequestBody PaymentProviderRequest paymentRequest) {
        try {
            PaymentResponse response = applicationService.payApplicationFee(applicationId, paymentRequest);
            ApiResponse<PaymentResponse> apiResponse = new ApiResponse<>("Payment initiated successfully", true, response);
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (CustomNotFoundException e) {
            ApiResponse<PaymentResponse> apiResponse = new ApiResponse<>("Application not found", false, null);
            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        } catch (IllegalStateException | PaymentProcessingException e) {
            ApiResponse<PaymentResponse> apiResponse = new ApiResponse<>("Payment processing failed", false, null);
            return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            ApiResponse<PaymentResponse> apiResponse = new ApiResponse<>("Unexpected error occurred", false, null);
            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }
    }

    /**
     * Endpoint to review an admission application.
     */
    @PutMapping("/applications/{applicationId}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ApplicationResponse>> reviewApplication(
            @PathVariable Long applicationId,
            @Valid @RequestBody ApplicationReviewDto review) {
        try {
            ApplicationResponse response = applicationService.reviewApplication(applicationId, review);
            ApiResponse<ApplicationResponse> apiResponse = new ApiResponse<>("Application reviewed successfully", true, response);
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<ApplicationResponse> apiResponse = new ApiResponse<>(e.getMessage(), false, null);
            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }
    }
}
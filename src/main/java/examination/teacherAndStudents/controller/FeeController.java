package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.FeeDTO;
import examination.teacherAndStudents.dto.FeeResponseDto;
import examination.teacherAndStudents.dto.StudentFeeResponse;
import examination.teacherAndStudents.entity.Fee;
import examination.teacherAndStudents.service.FeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for handling fee-related endpoints.
 */
@RestController
@RequestMapping("/api/v1/fees")
@RequiredArgsConstructor
public class FeeController {

    private final FeeService feeService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<FeeResponseDto>> createFee(@Valid @RequestBody FeeDTO feeDTO) {
        try {
            FeeResponseDto createdFee = feeService.createFee(feeDTO);
            return ResponseEntity.ok(new ApiResponse<>("Fee created successfully", true, createdFee));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("Failed to create fee: " + e.getMessage(), false));
        }
    }

    @GetMapping("/applicable/{studentId}")
    public ResponseEntity<ApiResponse<List<StudentFeeResponse>>> getApplicableFees(@PathVariable Long studentId) {
        try {
            List<StudentFeeResponse> fees = feeService.getApplicableFeesForStudent(studentId);
            return ResponseEntity.ok(new ApiResponse<>(
                    "Applicable fees retrieved successfully",
                    true,
                    fees
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("Failed to retrieve applicable fees: " + e.getMessage(), false));
        }
    }

    @GetMapping("/unpaid/{studentId}")
    public ResponseEntity<ApiResponse<List<StudentFeeResponse>>> getUnpaidFees(@PathVariable Long studentId) {
        try {
            List<StudentFeeResponse> unpaidFees = feeService.getApplicableUnpaidFeesForStudent(studentId);
            return ResponseEntity.ok(new ApiResponse<>(
                    "Unpaid fees retrieved successfully",
                    true,
                    unpaidFees
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("Failed to retrieve unpaid fees: " + e.getMessage(), false));
        }
    }

    @GetMapping("/application-fee")
    public ResponseEntity<ApiResponse<BigDecimal>> getApplicationFee(
            @RequestParam Long schoolId,
            @RequestParam(required = false) Long classLevelId,
            @RequestParam(required = false) Long subClassId
    ) {
        try {
            BigDecimal fee = feeService.getApplicationFee(schoolId, classLevelId, subClassId);
            return ResponseEntity.ok(new ApiResponse<>(
                    "Application fee retrieved successfully",
                    true,
                    fee
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("Failed to retrieve application fee: " + e.getMessage(), false));
        }
    }
}
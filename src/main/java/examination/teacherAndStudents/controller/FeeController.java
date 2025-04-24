package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.FeeDTO;
import examination.teacherAndStudents.dto.StudentFeeResponse;
import examination.teacherAndStudents.entity.Fee;
import examination.teacherAndStudents.service.FeeService;
import examination.teacherAndStudents.dto.FeeResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller for handling fee-related endpoints.
 */
@RestController
@RequestMapping("/api/v1/fees")
@RequiredArgsConstructor
public class FeeController {

    private final FeeService feeService;

    @PostMapping("/create")
    public ResponseEntity<FeeResponseDto> createFee(@Valid @RequestBody FeeDTO feeDTO) {
        FeeResponseDto createdFee = feeService.createFee(feeDTO);
        return ResponseEntity.ok(createdFee);
    }

    @GetMapping("/applicable/{studentId}")
    public ResponseEntity<List<Fee>> getApplicableFees(@PathVariable Long studentId) {
        List<Fee> fees = feeService.getApplicableFeesForStudent(studentId);
        return ResponseEntity.ok(fees);
    }

    @GetMapping("/unpaid/{studentId}")
    public ResponseEntity<List<StudentFeeResponse>> getUnpaidFees(@PathVariable Long studentId) {
        List<StudentFeeResponse> unpaidFees = feeService.getApplicableUnpaidFeesForStudent(studentId);
        return ResponseEntity.ok(unpaidFees);
    }

    @GetMapping("/application-fee")
    public ResponseEntity<BigDecimal> getApplicationFee(
            @RequestParam Long schoolId,
            @RequestParam(required = false) Long classLevelId,
            @RequestParam(required = false) Long subClassId
    ) {
        BigDecimal fee = feeService.getApplicationFee(schoolId, classLevelId, subClassId);
        return ResponseEntity.ok(fee);
    }
}
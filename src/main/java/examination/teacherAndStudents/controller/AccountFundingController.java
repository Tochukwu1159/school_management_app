package examination.teacherAndStudents.controller;


import examination.teacherAndStudents.dto.AccountFundingRequest;
import examination.teacherAndStudents.dto.AccountFundingResponse;
import examination.teacherAndStudents.service.AccountFundingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class AccountFundingController {

    private final AccountFundingService accountFundingService;

    @PostMapping("/initialize")
    public ResponseEntity<AccountFundingResponse> initializePayment(@RequestBody AccountFundingRequest request) throws Exception {
        return ResponseEntity.ok(accountFundingService.initializePayment(request));
    }

    @GetMapping("/callback")
    public ResponseEntity<String> paymentCallback(@RequestParam String reference) {
        // Frontend can verify payment status via API
        return ResponseEntity.ok("Payment processing. Reference: " + reference);
    }
}
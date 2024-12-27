package examination.teacherAndStudents.paystack;

import examination.teacherAndStudents.entity.StaffPayroll;
import examination.teacherAndStudents.repository.StaffPayrollRepository;
import examination.teacherAndStudents.utils.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static examination.teacherAndStudents.utils.AccountUtils.PAYSTACK_BULK_TRANSFER_URL;

@RequiredArgsConstructor
@Service
public class PayStackBulkTransfer {

    private final RestTemplate restTemplate;
    private final StaffPayrollRepository staffPayrollRepository;

    @Value("${paystack_secret_key}")
    private String payStackSecretKey;

    public String payStaff(Long schoolId, int month, int year) {
        // Fetch the payrolls for the given schoolId, month, and year
        List<StaffPayroll> payrolls = staffPayrollRepository.findBySchoolIdAndYearAndMonthAndStatus(schoolId, year, month, PaymentStatus.PENDING);

        // Check if there are any payrolls to process
        if (payrolls.isEmpty()) {
            return "No payrolls found for the given criteria.";
        }

        // Prepare the payment payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("source", "balance"); // Paystack supports 'balance' or 'card' as source

        // Prepare transfer details
        List<Map<String, Object>> transfers = payrolls.stream()
                .filter(payroll -> payroll.getNetPay() > 0 && payroll.getStaff().getBankAccountId() != null) // Ensure valid net pay and bank account ID
                .map(payroll -> {
                    Map<String, Object> transfer = new HashMap<>();
                    transfer.put("amount", (int) (payroll.getNetPay() * 100)); // Convert to kobo (smallest unit of Naira)
                    transfer.put("recipient", payroll.getStaff().getBankAccountId()); // Ensure staff has a valid Paystack recipient code
                    transfer.put("reason", "Salary Payment for " + payroll.getName());
                    return transfer;
                })
                .collect(Collectors.toList());

        if (transfers.isEmpty()) {
            return "No valid payrolls to transfer.";
        }

        payload.put("transfers", transfers);

        // Send bulk transfer request to Paystack
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + payStackSecretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    PAYSTACK_BULK_TRANSFER_URL, HttpMethod.POST, request, String.class);

            // Check if the response is successful
            if (response.getStatusCode() == HttpStatus.OK) {
                payrolls.forEach(payroll -> {
                    payroll.setDatePayed(LocalDateTime.now());
                    payroll.setPaymentStatus(PaymentStatus.SUCCESS);
                });
                staffPayrollRepository.saveAll(payrolls);

                return "Payment processed successfully!";
            } else {
                return "Failed to process payment: " + response.getBody();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error processing payment: " + e.getMessage());
        }
    }
}

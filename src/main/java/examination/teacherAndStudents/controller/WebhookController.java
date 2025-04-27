package examination.teacherAndStudents.controller;

import com.google.gson.Gson;
import examination.teacherAndStudents.repository.AccountFundingRepository;
import examination.teacherAndStudents.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.security.MessageDigest;
import java.util.Map;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class WebhookController {
    private static final Logger logger = LoggerFactory.getLogger(ComplaintController.class);

    private final AccountFundingRepository transactionRepository;
    private final WalletService walletService;
    private final Gson gson;

    @Value("${app.webhook.secret}")
    private String webhookSecret;

    @PostMapping("/paystack")
    public ResponseEntity<?> handlePayStackWebhook(@RequestBody String payload, HttpServletRequest request) throws Exception {
        String signature = request.getHeader("x-paystack-signature");
        if (!verifyPayStackSignature(payload, signature)) {
            return ResponseEntity.status(400).body("Invalid signature");
        }

        Map data = gson.fromJson(payload, Map.class);
        String event = (String) data.get("event");
        if ("charge.success".equals(event)) {
            Map<String, Object> eventData = (Map<String, Object>) data.get("data");
            String reference = (String) eventData.get("reference");
            updateTransaction(reference, (String) eventData.get("paid_at"));
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/flutterwave")
    public ResponseEntity<?> handleFlutterWaveWebhook(@RequestBody Map<String, Object> payload) {
        String status = (String) payload.get("status");
        if ("successful".equals(status)) {
            String txRef = (String) payload.get("tx_ref");
            String paidAt = (String) payload.get("created_at");
            updateTransaction(txRef, paidAt);
        }

        return ResponseEntity.ok().build();
    }

    private boolean verifyPayStackSignature(String payload, String signature) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        digest.update((payload + webhookSecret).getBytes());
        String computed = bytesToHex(digest.digest());
        return computed.equals(signature);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    private void updateTransaction(String reference, String paidAt) {
        transactionRepository.findByReference(reference).ifPresent(transaction -> {
            transaction.setStatus("SUCCESS");
            transaction.setPaidAt(java.time.LocalDateTime.parse(paidAt));
            transactionRepository.save(transaction);

            try {
                walletService.fundWallet1(transaction.getAmount(), transaction.getStudent());
            } catch (Exception e) {
                logger.error("Failed to update wallet for transaction {}: {}", reference, e.getMessage());
            }
        });
    }
}
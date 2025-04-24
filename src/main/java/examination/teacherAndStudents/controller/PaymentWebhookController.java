package examination.teacherAndStudents.controller;
import examination.teacherAndStudents.dto.WebhookRequest;
import examination.teacherAndStudents.service.WalletService;
import examination.teacherAndStudents.service.funding.PaymentProvider;
import examination.teacherAndStudents.service.funding.PaymentProviderFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments/webhook")
@RequiredArgsConstructor
public class PaymentWebhookController {
    private final WalletService walletService;
    private final PaymentProviderFactory providerFactory;

    @PostMapping("/{provider}")
    public ResponseEntity<String> handleWebhook(
            @PathVariable String provider,
            @RequestBody String payload,
            @RequestHeader("X-Signature") String signature) {

        WebhookRequest request = new WebhookRequest(payload, signature, provider);
        walletService.handlePaymentWebhook(request);

        return ResponseEntity.ok("Webhook processed successfully");
    }


    @RestController
    @RequestMapping("/api/webhooks/stripe")
    public class StripeWebhookController {


        @PostMapping
        public ResponseEntity<String> handleWebhook(
                @RequestBody String payload,
                @RequestHeader("Stripe-Signature") String signature) {

            WebhookRequest request = new WebhookRequest(payload, signature, "stripe");
            PaymentProvider provider = providerFactory.getProviderForWebhook("stripe");
            provider.handleWebhook(request);

            return ResponseEntity.ok("Webhook processed");
        }
    }

    @RestController
    @RequestMapping("/api/webhooks/paystack")
    public class PaystackWebhookController {

        @PostMapping
        public ResponseEntity<String> handleWebhook(
                @RequestBody String payload,
                @RequestHeader("x-paystack-signature") String signature) {

            WebhookRequest request = new WebhookRequest(payload, signature, "paystack");
            PaymentProvider provider = providerFactory.getProviderForWebhook("paystack");
            provider.handleWebhook(request);

            return ResponseEntity.ok("Webhook processed successfully");
        }
    }


    @RestController
    @RequestMapping("/api/webhooks/flutterwave")
    public class FlutterwaveWebhookController {

        @PostMapping
        public ResponseEntity<String> handleWebhook(
                @RequestBody String payload,
                @RequestHeader("verif-hash") String signature) {

            WebhookRequest request = new WebhookRequest(payload, signature, "flutterwave");
            PaymentProvider provider = providerFactory.getProviderForWebhook("flutterwave");
            provider.handleWebhook(request);

            return ResponseEntity.ok("Webhook processed");
        }
    }
}
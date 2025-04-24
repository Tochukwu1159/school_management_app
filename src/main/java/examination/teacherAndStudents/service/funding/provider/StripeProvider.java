//package examination.teacherAndStudents.service.funding.provider;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.stripe.exception.SignatureVerificationException;
//import com.stripe.exception.StripeException;
//import com.stripe.model.Charge;
//import com.stripe.model.Customer;
//import com.stripe.model.Event;
//import com.stripe.model.PaymentIntent;
//import com.stripe.net.Webhook;
//import examination.teacherAndStudents.dto.*;
//import examination.teacherAndStudents.error_handler.PaymentProcessingException;
//import examination.teacherAndStudents.service.PaymentService;
//import examination.teacherAndStudents.service.WalletService;
//import examination.teacherAndStudents.service.funding.BasePaymentProvider;
//import lombok.RequiredArgsConstructor;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.client.RestTemplate;
//
//import java.math.BigDecimal;
//import java.time.Instant;
//import java.time.ZoneId;
//import java.util.Map;
//
//@Service
//public class StripeProvider extends BasePaymentProvider {
//    private static final String BASE_URL = "https://api.stripe.com/v1";
//    private static final Logger logger = LoggerFactory.getLogger(StripeProvider.class);
//    private  final WalletService walletService;
//    private final PaymentService paymentService;
//
//
//    public StripeProvider(RestTemplate restTemplate, ObjectMapper objectMapper, @Value("${stripe.secret-key}") String stripeSecretKey, WalletService walletService, PaymentService paymentService) {
//        super(restTemplate, objectMapper, stripeSecretKey);
//        this.walletService = walletService;
//        this.paymentService = paymentService;
//    }
//
//    @Override
//    public PaymentInitResponse initiatePayment(PaymentRequestDto request) {
//        try {
//            HttpHeaders headers = createHeaders();
//            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//
//            MultiValueMap<String, String> stripeRequest = new LinkedMultiValueMap<>();
//            stripeRequest.add("amount", request.getAmount().multiply(BigDecimal.valueOf(100)).intValue() + "");
//            stripeRequest.add("currency", "usd");
//            stripeRequest.add("customer_email", request.getEmail());
//            stripeRequest.add("success_url", request.getCallbackUrl());
//            stripeRequest.add("cancel_url", request.getCallbackUrl());
//
//            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(stripeRequest, headers);
//
//            ResponseEntity<StripePaymentResponse> response = restTemplate.exchange(
//                    BASE_URL + "/checkout/sessions",
//                    HttpMethod.POST,
//                    entity,
//                    StripePaymentResponse.class);
//
//            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
//                StripePaymentResponse stripeResponse = response.getBody();
//
//                return PaymentInitResponse.builder().status( "open".equalsIgnoreCase(stripeResponse.getStatus())).authorizationUrl(stripeResponse.getSuccessUrl()).message( "Payment initiated successfully").id(stripeResponse.getId()).build();
//
//            }
//            throw new PaymentProcessingException("Stripe initialization failed");
//        } catch (Exception e) {
//            throw new PaymentProcessingException("Error initiating Stripe payment "+ e);
//        }
//    }
//
//    @Override
//    public PaymentVerificationResponse verifyPayment(String reference) {
//        try {
//            HttpHeaders headers = createHeaders();
//            HttpEntity<?> entity = new HttpEntity<>(headers);
//
//            ResponseEntity<StripeVerificationResponse> response = restTemplate.exchange(
//                    BASE_URL + "/checkout/sessions/" + reference,
//                    HttpMethod.GET,
//                    entity,
//                    StripeVerificationResponse.class);
//
//            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
//                return convertToVerificationResponse(response.getBody());
//            }
//            throw new PaymentProcessingException("Stripe verification failed");
//        } catch (Exception e) {
//            throw new PaymentProcessingException("Error verifying Stripe payment " + e);
//        }
//    }
//
//    @Override
//    public boolean supportsWebhook(String provider) {
//        return "stripe".equalsIgnoreCase(provider);
//    }
//
//    @Override
//    public void handleWebhook(WebhookRequest request) {
//        try {
//            // 1. Verify the webhook signature
//            String signatureHeader = request.getSignature();
//            String payload = request.getPayload();
//
//            Event event = constructEvent(payload, signatureHeader);
//
//            // 2. Process based on event type
//            switch (event.getType()) {
//                case "payment_intent.succeeded":
//                    handleSuccessfulPayment(event);
//                    break;
//
//                case "payment_intent.payment_failed":
//                    handleFailedPayment(event);
//                    break;
//
//                case "charge.refunded":
//                    handleRefund(event);
//                    break;
//
//                default:
//                    logger.info("Unhandled Stripe event type: {}", event.getType());
//            }
//        } catch (SignatureVerificationException e) {
//            logger.error("Stripe webhook signature verification failed", e);
//            throw new SecurityException("Invalid webhook signature", e);
//        } catch (Exception e) {
//            logger.error("Error processing Stripe webhook", e);
//            throw new PaymentProcessingException("Webhook processing failed "+ e);
//        }
//    }
////stripeWebhookSecret
//    private Event constructEvent(String payload, String sigHeader) throws StripeException {
//        return Webhook.constructEvent(
//                payload,
//                sigHeader,
//                "hhjhhhh"
//        );
//    }
//
//    private void handleSuccessfulPayment(Event event) {
//        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElseThrow();
//        String paymentId = paymentIntent.getId();
//        BigDecimal amount = BigDecimal.valueOf(paymentIntent.getAmount())
//                .divide(BigDecimal.valueOf(100)); // Convert from cents
//
//        // Get customer email from metadata or payment method
//        String email = paymentIntent.getMetadata().get("customer_email");
//        if (email == null && paymentIntent.getCustomer() != null) {
//            try {
//                Customer customer = Customer.retrieve(paymentIntent.getCustomer());
//                email = customer.getEmail();
//            } catch (StripeException e) {
//                logger.warn("Could not retrieve customer email", e);
//            }
//        }
//
//        try {
//            walletService.creditWalletFromWebhook(
//                    paymentId,
//                    amount,
//                    paymentIntent.getCurrency(),
//                    email,
//                    "Stripe payment completed"
//            );
//            logger.info("Processed successful Stripe payment: {}", paymentId);
//        } catch (Exception e) {
//            logger.error("Failed to process Stripe payment {}", paymentId, e);
//            throw e;
//        }
//    }
//
//    private void handleFailedPayment(Event event) {
//        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElseThrow();
//        String failureMessage = paymentIntent.getLastPaymentError() != null ?
//                paymentIntent.getLastPaymentError().getMessage() : "Unknown failure reason";
//        String email = paymentIntent.getMetadata().get("customer_email");
//
//        paymentService.recordFailedPayment(
//                paymentIntent.getId(),
//                failureMessage,
//                "stripe",
//                email
//        );
//
//        logger.warn("Stripe payment failed: {} - {}", paymentIntent.getId(), failureMessage);
//    }
//
//    private void handleRefund(Event event) {
//        Charge charge = (Charge) event.getDataObjectDeserializer().getObject().orElseThrow();
//        // Implement refund logic
//        logger.info("Processing refund for charge: {}", charge.getId());
//    }
//    private PaymentVerificationResponse convertToVerificationResponse(StripeVerificationResponse stripeResponse) {
//        if (stripeResponse == null) {
//            return PaymentVerificationResponse.failed("Invalid Stripe verification response");
//        }
//
//        return PaymentVerificationResponse.builder()
//                .success("paid".equalsIgnoreCase(stripeResponse.getPaymentStatus()))
//                .transactionId(stripeResponse.getId())
//                .reference(stripeResponse.getPaymentIntent())
//                .paymentMethod("card") // Stripe primarily handles card payments
//                .amount(BigDecimal.valueOf(stripeResponse.getAmount())
//                        .divide(BigDecimal.valueOf(100))) // Convert from cents
//                .currency(stripeResponse.getCurrency())
//                .paymentDate(Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime())
//                .customerEmail(stripeResponse.getCustomerDetails() != null ?
//                        stripeResponse.getCustomerDetails().getEmail() : null)
//                .statusMessage(stripeResponse.getStatus())
//                .provider("stripe")
//                .metadata(Map.of(
//                        "payment_status", stripeResponse.getPaymentStatus()
//                ))
//                .build();
//    }
//}

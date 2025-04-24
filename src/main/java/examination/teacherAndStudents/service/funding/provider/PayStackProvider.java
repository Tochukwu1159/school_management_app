//package examination.teacherAndStudents.service.funding.provider;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import examination.teacherAndStudents.dto.*;
//import examination.teacherAndStudents.error_handler.PaymentProcessingException;
//import examination.teacherAndStudents.service.PaymentService;
//import examination.teacherAndStudents.service.TransactionService;
//import examination.teacherAndStudents.service.TransferService;
//import examination.teacherAndStudents.service.WalletService;
//import examination.teacherAndStudents.service.funding.BasePaymentProvider;
//import org.apache.commons.codec.binary.Hex;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import javax.crypto.Mac;
//import javax.crypto.spec.SecretKeySpec;
//import java.math.BigDecimal;
//import java.nio.charset.StandardCharsets;
//import java.time.ZoneId;
//import java.util.Map;
//
//@Service
//public class PayStackProvider extends BasePaymentProvider {
//    private static final String BASE_URL = "https://api.paystack.co";
//    private static final String PAYSTACK_WEBHOOK_EVENT = "event";
//    private static final String PAYSTACK_WEBHOOK_DATA = "data";
//    private static final String PAYSTACK_SUCCESS_EVENT = "charge.success";
//    private  final WalletService walletService;
//    private final TransferService transferService;
//
//    private static final Logger logger = LoggerFactory.getLogger(PayStackProvider.class);
//    private final PaymentService paymentService;
//
//
//    public PayStackProvider(RestTemplate restTemplate,
//                            ObjectMapper objectMapper,
//                            @Value("${paystack_secret_key}") String paystackSecretKey, WalletService walletService, TransferService transferService, PaymentService paymentService) {
//        super(restTemplate, objectMapper, paystackSecretKey);
//        this.walletService = walletService;
//        this.transferService = transferService;
//        this.paymentService = paymentService;
//    }
//
//    @Override
//    public PaymentInitResponse initiatePayment(PaymentRequestDto request) {
//        try {
//            HttpHeaders headers = createHeaders();
//
//            PayStackInitRequest paystackRequest = PayStackInitRequest.builder().email(request.getEmail())
//                    .amount(request.getAmount()
//                            .multiply(BigDecimal.valueOf(100)))
//                    .metadata(request.getMetadata())
//                    .callbackUrl(request.getCallbackUrl()).build();
//
//            HttpEntity<PayStackInitRequest> entity = new HttpEntity<>(paystackRequest, headers);
//
//            ResponseEntity<PayStackInitResponse> response = restTemplate.exchange(
//                    BASE_URL + "/transaction/initialize",
//                    HttpMethod.POST,
//                    entity,
//                    PayStackInitResponse.class);
//
//            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
//                PayStackInitResponse paystackResponse = response.getBody();
//
//                PaymentInitResponse.builder().status(paystackResponse.isStatus()).authorizationUrl(paystackResponse.getData()
//                        .getAuthorizationUrl()).message(paystackResponse.getMessage()).reference(paystackResponse.getData().getReference()).build();
//
//            }
//            throw new PaymentProcessingException("PayStack initialization failed");
//        } catch (Exception e) {
//            throw new PaymentProcessingException("Error initiating PayStack payment "+ e);
//        }
//    }
//
//    @Override
//    public PaymentVerificationResponse verifyPayment(String reference) {
//        try {
//            HttpHeaders headers = createHeaders();
//            HttpEntity<?> entity = new HttpEntity<>(headers);
//
//            ResponseEntity<PayStackVerificationResponse> response = restTemplate.exchange(
//                    BASE_URL + "/transaction/verify/" + reference,
//                    HttpMethod.GET,
//                    entity,
//                    PayStackVerificationResponse.class);
//
//            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
//                return convertToVerificationResponse(response.getBody());
//            }
//            throw new PaymentProcessingException("PayStack verification failed");
//        } catch (Exception e) {
//            throw new PaymentProcessingException("Error verifying PayStack payment "+ e);
//        }
//    }
//
//    @Override
//    public boolean supportsWebhook(String provider) {
//        return "paystack".equalsIgnoreCase(provider);
//    }
//
//    @Override
//    public void handleWebhook(WebhookRequest request) {
//        try {
//            // 1. Verify the webhook signature
//            verifyPaystackSignature(request.getPayload(), request.getSignature());
//
//            // 2. Parse the webhook payload into DTO
//            PaystackWebhookPayload payload = objectMapper.readValue(
//                    request.getPayload(),
//                    PaystackWebhookPayload.class
//            );
//
//            // 3. Process based on event type
//            switch (payload.getEvent()) {
//                case PAYSTACK_SUCCESS_EVENT: // "charge.success"
//                    handleSuccessfulPayment(payload);
//                    break;
//
//                case "charge.failed":
//                    handleFailedPayment(payload);
//                    break;
//
//                case "transfer.failed":
//                    handleFailedTransfer(payload);
//                    break;
//
//                default:
//                    logger.info("Received unhandled Paystack event: {}", payload.getEvent());
//            }
//        } catch (SecurityException e) {
//            logger.error("Paystack webhook signature verification failed", e);
//            throw e;
//        } catch (Exception e) {
//            logger.error("Error processing Paystack webhook", e);
//            throw new PaymentProcessingException("Webhook processing failed "+ e);
//        }
//    }
//
//    private void handleFailedPayment(PaystackWebhookPayload payload) {
//        // 1. Extract failure details
//        String reference = payload.getData().getReference();
//        String failureReason = payload.getData().getGatewayResponse();
//        String customerEmail = payload.getData().getCustomer() != null ?
//                payload.getData().getCustomer().getEmail() : null;
//
//        // 2. Record the failed payment
//        try {
//            paymentService.recordFailedPayment(
//                    reference,
//                    failureReason,
//                    "paystack",
//                    customerEmail
//            );
//
//            logger.warn("Paystack payment failed - Reference: {}, Reason: {}",
//                    reference, failureReason);
//
//        } catch (Exception e) {
//            logger.error("Failed to record failed Paystack payment: {}", reference, e);
//            throw e;
//        }
//    }
//
//    private void verifyPaystackSignature(String payload, String signature) {
//        try {
////            this is paystackSecretKey
//            String computedSignature = HmacUtil.calculateHMAC512(payload, "paystack key");
//            if (!computedSignature.equals(signature)) {
//                throw new SecurityException("Invalid Paystack webhook signature");
//            }
//        } catch (Exception e) {
//            throw new SecurityException("Error verifying signature", e);
//        }
//    }
//
//    private void handleFailedTransfer(PaystackWebhookPayload payload) {
//        String transferId = String.valueOf(payload.getData().getId());
//        String failureReason = payload.getData().getGatewayResponse(); // Paystack's failure message
//
//        try {
//            transferService.recordFailedTransfer(
//                    transferId,
//                    failureReason,
//                    "paystack"
//            );
//
//            logger.warn("Paystack transfer failed - ID: {}, Reason: {}",
//                    transferId, failureReason);
//
//        } catch (Exception e) {
//            logger.error("Failed to record failed Paystack transfer: {}", transferId, e);
//            throw e;
//        }
//    }
//
//    private void handleSuccessfulPayment(PaystackWebhookPayload payload) {
//        String reference = payload.getData().getReference();
//        BigDecimal amount = BigDecimal.valueOf(payload.getData().getAmount()).divide(BigDecimal.valueOf(100)); // Convert from kobo
//        String email = payload.getData().getCustomer() != null ?
//                payload.getData().getCustomer().getEmail() : null;
//        String gatewayResponse = payload.getData().getGatewayResponse();
//
//        // 2. Verify transaction is truly successful
//        if (!"success".equalsIgnoreCase(payload.getData().getStatus())) {
//            logger.warn("Received success event for non-successful transaction: {}", reference);
//            return;
//        }
//
//        // 3. Process the successful payment
//        try {
//            if (email == null) {
//                throw new PaymentProcessingException("Customer email is null for reference: " + reference);
//            }
//
//            walletService.creditWalletFromWebhook(
//                    reference,
//                    amount,
//                    "NGN", // Paystack defaults to Naira
//                    email,
//                    gatewayResponse
//            );
//            logger.info("Successfully processed Paystack payment for reference: {}", reference);
//        } catch (Exception e) {
//            logger.error("Failed to process Paystack payment for reference: {}", reference, e);
//            throw new PaymentProcessingException("Payment processing failed for reference: " + reference+ " "+ e);
//        }
//    }
//
//    // HMAC Utility Class
//    public class HmacUtil {
//        public static String calculateHMAC512(String data, String key) throws Exception {
//            Mac sha512_HMAC = Mac.getInstance("HmacSHA512");
//            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
//            sha512_HMAC.init(secret_key);
//            byte[] hash = sha512_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
//            return Hex.encodeHexString(hash);
//        }
//    }
//
//    private PaymentVerificationResponse convertToVerificationResponse(PayStackVerificationResponse paystackResponse) {
//        if (paystackResponse == null || !paystackResponse.isStatus() || paystackResponse.getData() == null) {
//            return PaymentVerificationResponse.failed(
//                    paystackResponse != null ? paystackResponse.getMessage() : "Invalid Paystack verification response"
//            );
//        }
//
//        PayStackVerificationResponse.TransactionData data = paystackResponse.getData();
//        PayStackVerificationResponse.Customer customer = data.getCustomer();
//        PayStackVerificationResponse.Authorization authorization = data.getAuthorization();
//
//        return PaymentVerificationResponse.builder()
//                .success("success".equalsIgnoreCase(data.getStatus()))
//                .transactionId(String.valueOf(paystackResponse.getData().getId()))
//                .reference(data.getReference())
//                .paymentMethod(authorization != null ? authorization.getChannel() : "unknown")
//                .amount(BigDecimal.valueOf(data.getAmount()).divide(BigDecimal.valueOf(100))) // Convert from kobo
//                .currency(data.getCurrency())
//                .paymentDate(data.getPaidAt() != null ?
//                        data.getPaidAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() :
//                        null)
//                .customerEmail(customer != null ? customer.getEmail() : null)
//                .statusMessage(data.getGatewayResponse())
//                .provider("paystack")
//                .metadata(Map.of(
//                        "authorization_code", authorization != null ? authorization.getAuthorizationCode() : "",
//                        "bank", authorization != null ? authorization.getBank() : "",
//                        "card_type", authorization != null ? authorization.getCardType() : "",
//                        "ip_address", data.getIpAddress(),
//                        "fees", data.getFees(),
//                        "raw_response", objectMapper.valueToTree(paystackResponse)
//                ))
//                .build();
//    }}
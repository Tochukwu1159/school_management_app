//package examination.teacherAndStudents.service.funding.provider;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import examination.teacherAndStudents.Security.SecurityConfig;
//import examination.teacherAndStudents.dto.*;
//import examination.teacherAndStudents.entity.Profile;
//import examination.teacherAndStudents.error_handler.CustomNotFoundException;
//import examination.teacherAndStudents.error_handler.PaymentProcessingException;
//import examination.teacherAndStudents.repository.ProfileRepository;
//import examination.teacherAndStudents.service.PaymentService;
//import examination.teacherAndStudents.service.TransferService;
//import examination.teacherAndStudents.service.WalletService;
//import examination.teacherAndStudents.service.funding.BasePaymentProvider;
//import examination.teacherAndStudents.utils.PaymentMethod;
//import lombok.*;
//import org.apache.commons.codec.digest.HmacUtils;
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
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.Map;
//
//
//
//@Service
//public class FlutterwaveProvider extends BasePaymentProvider {
//    private static final String BASE_URL = "https://api.flutterwave.com/v3";
//    private final ProfileRepository profileRepository;
//    private  final WalletService walletService;
//    private final PaymentService paymentService;
//    private final TransferService transferService;
//
//
//    private static final Logger logger = LoggerFactory.getLogger(FlutterwaveProvider.class);
//
//
//    public FlutterwaveProvider(RestTemplate restTemplate, ObjectMapper objectMapper, ProfileRepository profileRepository, @Value("${flutterwave.secret-key}") String flutterwaveSecretKey, WalletService walletService, PaymentService paymentService, TransferService transferService) {
//        super(restTemplate, objectMapper, flutterwaveSecretKey);
//        this.profileRepository = profileRepository;
//        this.walletService = walletService;
//        this.paymentService = paymentService;
//        this.transferService = transferService;
//    }
//
//
//    @Override
//    public PaymentInitResponse initiatePayment(PaymentRequestDto request) {
//        try {
//            HttpHeaders headers = createHeaders();
//
//            String email = SecurityConfig.getAuthenticatedUserEmail();
//            Profile user = profileRepository.findByUserEmail(email)
//                    .orElseThrow(() -> new CustomNotFoundException("User profile not found"));
//
//            String transactionRef = "FLW-" + System.currentTimeMillis() + "-" +
//                    user.getUniqueRegistrationNumber().substring(0, 4);
//
//            FlutterwavePaymentRequest flutterwaveRequest = FlutterwavePaymentRequest.builder()
//                    .tx_ref(transactionRef)
//                    .amount(request.getAmount())
//                    .currency("NGN") // Default to Naira
//                    .redirect_url(request.getCallbackUrl())
//                    .payment_options("card,account,ussd")
//                    .customer(FlutterwavePaymentRequest.Customer.builder()
//                            .email(request.getEmail())
//                            .phonenumber(user.getPhoneNumber())
//                            .name(user.getUser().getFirstName() + " " + user.getUser().getLastName())
//                            .build())
//                    .customizations(Map.of(
//                            "title", "Your School Name",
//                            "description", "Wallet Funding",
//                            "logo", "https://your-school-logo.png"
//                    ))
//                    .build();
//
//            HttpEntity<FlutterwavePaymentRequest> entity = new HttpEntity<>(flutterwaveRequest, headers);
//
//            ResponseEntity<FlutterwavePaymentResponse> response = restTemplate.exchange(
//                    BASE_URL + "/payments",
//                    HttpMethod.POST,
//                    entity,
//                    FlutterwavePaymentResponse.class);
//
//            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
//                FlutterwavePaymentResponse flutterwaveResponse = response.getBody();
//                return PaymentInitResponse.builder()
//                        .status("success".equalsIgnoreCase(flutterwaveResponse.getStatus()))
//                        .authorizationUrl(flutterwaveResponse.getData().getLink())
//                        .reference(flutterwaveResponse.getData().getTransactionReference())
//                        .message(flutterwaveResponse.getMessage())
//                        .paymentMethod(PaymentMethod.FLUTTERWAVE)
//                        .providerResponse(objectMapper.writeValueAsString(flutterwaveResponse))
//                        .expiresAt(LocalDateTime.now().plusHours(1))
//              .build();
//            }
//            throw new PaymentProcessingException("Flutterwave initialization failed");
//        } catch (Exception e) {
//            throw new PaymentProcessingException("Error initiating Flutterwave payment " + e);
//        }
//    }
//
//    @Override
//    public PaymentVerificationResponse verifyPayment(String reference) {
//        try {
//            HttpHeaders headers = createHeaders();
//            HttpEntity<?> entity = new HttpEntity<>(headers);
//
//            ResponseEntity<FlutterwaveVerificationResponse> response = restTemplate.exchange(
//                    BASE_URL + "/transactions/" + reference + "/verify",
//                    HttpMethod.GET,
//                    entity,
//                    FlutterwaveVerificationResponse.class);
//
//            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
//                return convertToVerificationResponse(response.getBody());
//            }
//            throw new PaymentProcessingException("Flutterwave verification failed");
//        } catch (Exception e) {
//            throw new PaymentProcessingException("Error verifying Flutterwave payment "+ e);
//        }
//    }
//
//    @Override
//    public boolean supportsWebhook(String provider) {
//        return "flutterwave".equalsIgnoreCase(provider);
//    }
//
//    @Override
//    public void handleWebhook(WebhookRequest request) {
//        try {
//            // 1. Verify the webhook signature
//            String computedSignature = CryptoUtil.hmacSha256(
//                    request.getPayload(),
//""
////                    flutterwaveSecretKey
//            );
//
//            if (!computedSignature.equals(request.getSignature())) {
//                throw new SecurityException("Invalid webhook signature");
//            }
//
//            // 2. Parse the webhook payload
//            FlutterwaveWebhookPayload payload = objectMapper.readValue(
//                    request.getPayload(),
//                    FlutterwaveWebhookPayload.class
//            );
//
//            // 3. Process based on event type
//            switch (payload.getEvent()) {
//                case "charge.completed":
//                    handleSuccessfulPayment(payload);
//                    break;
//
//                case "charge.failed":
//                    handleFailedPayment(payload);
//                    break;
//
//                case "transfer.completed":
//                    handleTransferCompletion(payload);
//                    break;
//
//                default:
//                    logger.warn("Unhandled webhook event: {}", payload.getEvent());
//            }
//        } catch (Exception e) {
//            logger.error("Error processing Flutterwave webhook", e);
//            throw new PaymentProcessingException("Webhook processing failed "+ e);
//        }
//    }
//
//    private void handleSuccessfulPayment(FlutterwaveWebhookPayload payload) {
//        // 1. Verify transaction status
//        if (!"successful".equals(payload.getData().getStatus())) {
//            logger.warn("Received completed event for non-successful transaction");
//            return;
//        }
//
//        // 2. Update your system
//        String txRef = payload.getData().getReference();
//        BigDecimal amount = BigDecimal.valueOf(payload.getData().getAmount());
//        String currency = payload.getData().getCurrency();
//
//        // 3. Credit user's wallet
//        walletService.creditWalletFromWebhook(
//                txRef,
//                amount,
//                currency,
//                null,
//                payload.getData().getCustomer().getEmail()
//        );
//
//        // 4. Log the successful processing
//        logger.info("Processed successful payment for txRef: {}", txRef);
//    }
//
//
//
//    private void handleFailedPayment(FlutterwaveWebhookPayload payload) {
//        String txRef = payload.getData().getReference();
//        String failureMessage = payload.getData().getGatewayResponse();
//        String customer = payload.getData().getCustomer().getEmail();
//
//        // Update transaction status in your system
//        paymentService.recordFailedPayment(
//                txRef,
//                failureMessage,
//                "flutterwave",
//                customer
//
//        );
//
//        logger.warn("Payment failed for txRef: {}, reason: {}", txRef, failureMessage);
//    }
//
//    private void handleTransferCompletion(FlutterwaveWebhookPayload payload) {
//        try {
//            // 1. Extract transfer details
//            Long transferId = payload.getData().getId();
//            BigDecimal amount = BigDecimal.valueOf(payload.getData().getAmount());
//            String status = payload.getData().getStatus();
//            String currency = payload.getData().getCurrency();
//
//            // 2. Verify transfer is truly completed
//            if (!"completed".equalsIgnoreCase(status)) {
//                logger.warn("Received transfer.completed event for non-completed transfer: {}", transferId);
//                return;
//            }
//
//            // 3. Process the successful transfer
//            transferService.recordSuccessfulTransfer(
//                    String.valueOf(transferId),
//                    amount,
//                    currency,
//                    payload.getData().getCustomer().getEmail(),
//                    "Flutterwave transfer completed"
//            );
//
//            logger.info("Processed completed transfer: {}", transferId);
//
//        } catch (Exception e) {
//            logger.error("Failed to process transfer completion for payload: {}", payload, e);
//            throw new PaymentProcessingException("Transfer completion processing failed "+ e);
//        }
//    }
//
//
//    private PaymentVerificationResponse convertToVerificationResponse(FlutterwaveVerificationResponse flutterwaveResponse) {
//        if (flutterwaveResponse == null || flutterwaveResponse.getData() == null) {
//            return PaymentVerificationResponse.failed("Invalid Flutterwave verification response");
//        }
//
//        FlutterwaveVerificationResponse.VerificationData data = flutterwaveResponse.getData();
//        FlutterwaveVerificationResponse.Customer customer = data.getCustomer();
//
//        return PaymentVerificationResponse.builder()
//                .success("successful".equalsIgnoreCase(data.getStatus()))
//                .transactionId(data.getId())
//                .reference(data.getTransactionReference())
//                .paymentMethod(data.getPaymentType())
//                .amount(data.getAmount())
//                .currency(data.getCurrency())
//                .paymentDate(data.getCreatedAt())
//                .customerEmail(customer != null ? customer.getEmail() : null)
//                .statusMessage(flutterwaveResponse.getMessage())
//                .provider("flutterwave")
//                .metadata(Map.of(
//                        "flw_ref", data.getFlutterwaveReference(),
//                        "payment_status", data.getStatus()
//                ))
//                .build();
//    }
//
//    public static class CryptoUtil {
//        public static String hmacSha256(String data, String key) {
//            return HmacUtils.hmacSha256Hex(key, data);
//        }
//    }
//}

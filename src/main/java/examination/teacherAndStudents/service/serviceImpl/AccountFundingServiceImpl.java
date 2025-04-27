package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.AccountFundingRequest;
import examination.teacherAndStudents.dto.AccountFundingResponse;
import examination.teacherAndStudents.entity.AccountFunding;
import examination.teacherAndStudents.entity.PaymentAccount;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.School;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.AccountFundingRepository;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.service.AccountFundingService;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountFundingServiceImpl implements AccountFundingService {


    private final AccountFundingRepository accountFundingRepository;
    private final HttpClient httpClient = HttpClientBuilder.create().build();
    private final Gson gson = new Gson();
    private final ProfileRepository profileRepository;

    @Value("${paystack.secret.key}")
    private String paystackSecretKey;

    @Value("${paystack.base.url}")
    private String paystackBaseUrl;

    @Value("${flutterwave.secret.key}")
    private String flutterwaveSecretKey;

    @Value("${flutterwave.base.url}")
    private String flutterwaveBaseUrl;

    @Transactional
    public AccountFundingResponse initializePayment(AccountFundingRequest request) throws Exception {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        Profile profile = profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("User not found"));

        School school = profile.getUser().getSchool();
        PaymentAccount paymentAccount = school.getPaymentAccount();
        String reference = UUID.randomUUID().toString();

        AccountFunding transaction = AccountFunding.builder()
                .reference(reference)
                .amount(new BigDecimal(request.getAmount()))
                .status("PENDING")
                .gateway(request.getGateway())
                .student(profile)
                .school(school)
                .createdAt(java.time.LocalDateTime.now())
                .build();

        accountFundingRepository.save(transaction);

        if ("PAYSTACK".equalsIgnoreCase(request.getGateway())) {
            return initializePaystackPayment(request, paymentAccount, reference);
        } else if ("FLUTTERWAVE".equalsIgnoreCase(request.getGateway())) {
            return initializeFlutterwavePayment(request, paymentAccount, reference);
        } else {
            throw new IllegalArgumentException("Unsupported gateway: " + request.getGateway());
        }
    }

    private AccountFundingResponse initializePaystackPayment(AccountFundingRequest request, PaymentAccount paymentAccount, String reference) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", request.getEmail());
        payload.put("amount", new BigDecimal(request.getAmount()).multiply(new BigDecimal(100)).intValue()); // Paystack uses kobo
        payload.put("reference", reference);
        payload.put("subaccount", paymentAccount.getPaystackSubaccountCode());
        payload.put("callback_url", "http://your-domain.com/payment/callback");

        HttpPost post = new HttpPost(paystackBaseUrl + "/transaction/initialize");
        post.addHeader("Authorization", "Bearer " + paystackSecretKey);
        post.addHeader("Content-Type", "application/json");
        post.setEntity(new StringEntity(gson.toJson(payload)));

        String response = EntityUtils.toString(httpClient.execute(post).getEntity());
        Map responseMap = gson.fromJson(response, Map.class);

        if (responseMap.get("status") != null && (Boolean) responseMap.get("status")) {
            Map<String, String> data = (Map<String, String>) responseMap.get("data");
            return new AccountFundingResponse(data.get("authorization_url"), reference);
        } else {
            throw new RuntimeException("Paystack payment initialization failed: " + responseMap.get("message"));
        }
    }

    private AccountFundingResponse initializeFlutterwavePayment(AccountFundingRequest request, PaymentAccount paymentAccount, String reference) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("tx_ref", reference);
        payload.put("amount", request.getAmount());
        payload.put("currency", "NGN");
        payload.put("redirect_url", "http://your-domain.com/payment/callback");
        payload.put("customer", Map.of("email", request.getEmail()));
        payload.put("subaccounts", new Map[]{Map.of("id", paymentAccount.getFlutterwaveLinkedAccountId())});

        HttpPost post = new HttpPost(flutterwaveBaseUrl + "/payments");
        post.addHeader("Authorization", "Bearer " + flutterwaveSecretKey);
        post.addHeader("Content-Type", "application/json");
        post.setEntity(new StringEntity(gson.toJson(payload)));

        String response = EntityUtils.toString(httpClient.execute(post).getEntity());
        Map responseMap = gson.fromJson(response, Map.class);

        if ("success".equals(responseMap.get("status"))) {
            Map<String, String> data = (Map<String, String>) responseMap.get("data");
            return new AccountFundingResponse(data.get("link"), reference);
        } else {
            throw new RuntimeException("Flutterwave payment initialization failed: " + responseMap.get("message"));
        }
    }
}
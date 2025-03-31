package examination.teacherAndStudents.paystack;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import examination.teacherAndStudents.dto.PayStackErrorResponse;
import examination.teacherAndStudents.dto.PayStackTransactionRequest;
import examination.teacherAndStudents.dto.PayStackTransactionResponse;
import examination.teacherAndStudents.error_handler.PaymentProcessingException;
import examination.teacherAndStudents.service.PayStackPaymentService;
import examination.teacherAndStudents.utils.AccountUtils;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.tomcat.websocket.AuthenticationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Transactional
public class PayStackTransactionServiceImpl implements PayStackPaymentService {

    /**
     * PayStack secret key
     */
    @Value("${paystack_secret_key:paystack}")
    private String payStackSecretKey;
    private final PayStackVerification payStackVerification;


    public PayStackTransactionResponse initTransaction(PayStackTransactionRequest request) throws PaymentProcessingException {
        // Validate input
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        CloseableHttpClient client = null;
        try {
            // Create HTTP client with timeout configuration
            RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout(30_000)  // 30 seconds
                    .setSocketTimeout(30_000)    // 30 seconds
                    .build();

            client = HttpClientBuilder.create()
                    .setDefaultRequestConfig(config)
                    .build();

            // Prepare request
            HttpPost post = new HttpPost(AccountUtils.PAYSTACK_TRANSACTION_INITIALIZER);
            post.setHeader("Content-type", "application/json");
            post.setHeader("Authorization", "Bearer " + payStackSecretKey);

            // Add request payload
            Gson gson = new GsonBuilder().create();
            StringEntity entity = new StringEntity(gson.toJson(request), StandardCharsets.UTF_8);
            entity.setContentType("application/json");
            post.setEntity(entity);

            // Execute request
            try (CloseableHttpResponse response = client.execute(post)) {
                int statusCode = response.getStatusLine().getStatusCode();
                HttpEntity responseEntity = response.getEntity();
                String responseString = EntityUtils.toString(responseEntity);

                if (statusCode == HttpStatus.SC_OK) {
                    // Parse successful response
                    return gson.fromJson(responseString, PayStackTransactionResponse.class);
                } else {
                    // Parse error response
                    PayStackErrorResponse errorResponse = gson.fromJson(responseString, PayStackErrorResponse.class);
                    throw new PaymentProcessingException(
                            "PayStack API error: " + (errorResponse.getMessage() != null ?
                                    errorResponse.getMessage() : "Unknown error")
                    );
                }
            }
        } catch (JsonSyntaxException e) {
            throw new PaymentProcessingException("Failed to parse PayStack response "+ e);
        } catch (IOException e) {
            throw new PaymentProcessingException("Network error while calling PayStack API " + e);
        } finally {
            try {
                if (client != null) {
                    client.close();
                }
            } catch (IOException e) {
            }
        }
    }

//    public PayStackTransactionResponse initTransaction(PayStackTransactionRequest request) throws Exception {
//        try {
//            // Converts object to JSON
//            Gson gson = new Gson();
//            // Add PayStack charges to the amount
//            StringEntity postingString = new StringEntity(gson.toJson(request));
//
//            // Consuming PayStack API using HttpClient
//            HttpClient client = HttpClientBuilder.create().build();
//            HttpPost post = new HttpPost(AccountUtils.PAYSTACK_TRANSACTION_INITIALIZER);
//            post.setEntity(postingString);
//            post.addHeader("Content-type", "application/json");
//            post.addHeader("Authorization", "Bearer " + payStackSecretKey);
//
//            // Execute HTTP request
//            HttpResponse response = client.execute(post);
//            if (response.getStatusLine().getStatusCode() == 200) {
//                // Process the successful response
//                return handleSuccessfulResponse(response);
//            } else {
//                // Handle error response
//                handleErrorResponse(response);
//            }
//        } catch (Exception ex) {
//            // Log the exception for debugging
//            throw new Exception("Failure initializing PayStack transaction");
//        }
//        return null; // This line should not be reached, as exceptions are thrown
//    }

    private PayStackTransactionResponse handleSuccessfulResponse(HttpResponse response) throws IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        // Parse the response JSON
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(result.toString(), PayStackTransactionResponse.class);
    }

    private void handleErrorResponse(HttpResponse response) throws Exception {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 401) {
            // Unauthorized, handle accordingly
            throw new AuthenticationException("Unauthorized request to PayStack API");
        } else {
            // Handle other HTTP errors
            throw new Exception("Error occurred while initializing PayStack transaction. HTTP Status Code: " + statusCode);
        }
    }
}
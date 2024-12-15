//package examination.teacherAndStudents.service.paystack;
//
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.google.gson.Gson;
//import examination.teacherAndStudents.dto.PayStackTransactionRequest;
//import examination.teacherAndStudents.dto.PayStackTransactionResponse;
//import examination.teacherAndStudents.paystack.PayStackVerification;
//import examination.teacherAndStudents.service.PayStackPaymentService;
//import examination.teacherAndStudents.utils.AccountUtils;
//import lombok.RequiredArgsConstructor;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.client.HttpClientBuilder;
//import org.apache.tomcat.websocket.AuthenticationException;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class PayStackTransactionServiceImpl implements PayStackPaymentService {
//
//    /**
//     * PayStack secret key
//     */
//    @Value("${paystack_secret_key:paystack}")
//    private String payStackSecretKey;
//    private final PayStackVerification payStackVerification;
//
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
//            ex.printStackTrace();
//            throw new Exception("Failure initializing PayStack transaction");
//        }
//        return null; // This line should not be reached, as exceptions are thrown
//    }
//
//    private PayStackTransactionResponse handleSuccessfulResponse(HttpResponse response) throws IOException {
//        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
//        StringBuilder result = new StringBuilder();
//        String line;
//        while ((line = rd.readLine()) != null) {
//            result.append(line);
//        }
//
//        // Parse the response JSON
//        ObjectMapper mapper = new ObjectMapper();
//        return mapper.readValue(result.toString(), PayStackTransactionResponse.class);
//    }
//
//    private void handleErrorResponse(HttpResponse response) throws Exception {
//        int statusCode = response.getStatusLine().getStatusCode();
//        if (statusCode == 401) {
//            // Unauthorized, handle accordingly
//            throw new AuthenticationException("Unauthorized request to PayStack API");
//        } else {
//            // Handle other HTTP errors
//            throw new Exception("Error occurred while initializing PayStack transaction. HTTP Status Code: " + statusCode);
//        }
//    }
//}
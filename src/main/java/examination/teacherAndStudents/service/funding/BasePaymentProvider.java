package examination.teacherAndStudents.service.funding;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

public abstract class BasePaymentProvider implements PaymentProvider {
    protected final RestTemplate restTemplate;
    protected final ObjectMapper objectMapper;
    protected final String secretKey;

    public BasePaymentProvider(RestTemplate restTemplate, ObjectMapper objectMapper, String secretKey) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.secretKey = secretKey;
    }

    protected HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + secretKey);
        return headers;
    }
}

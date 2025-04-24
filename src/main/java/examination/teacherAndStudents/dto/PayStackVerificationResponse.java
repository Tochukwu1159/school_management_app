package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayStackVerificationResponse {
    private boolean status;
    private String message;
    private TransactionData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionData {
        private long id;
        private String reference;
        private long amount;
        private String currency;
        private String status;
        private String gatewayResponse;
        private String ipAddress;
        private Long fees;
        private Date paidAt;
        private Customer customer;
        private Authorization authorization;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Customer {
        private String email;
        private String firstName;
        private String lastName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Authorization {
        private String authorizationCode;
        private String channel;
        private String bank;
        private String cardType;
    }
}


package examination.teacherAndStudents.utils;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ReferenceGenerator {

    /**
     * Generates a standardized payment reference number
     * Format: PAY-{UUID}-{timestamp}
     */
    public static String generatePaymentReference() {
        return String.format("PAY-%s-%d",
                UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(),
                System.currentTimeMillis() % 1000000);
    }

    /**
     * Generates a transaction ID with custom prefix
     * @param prefix 2-4 character identifier (e.g., "MOB" for mobile money)
     */
    public static String generateTransactionId(String prefix) {
        if (prefix == null || prefix.length() < 2 || prefix.length() > 4) {
            throw new IllegalArgumentException("Prefix must be 2-4 characters");
        }
        return String.format("%s-%s-%s",
                prefix.toUpperCase(),
                UUID.randomUUID().toString().substring(0, 4),
                UUID.randomUUID().toString().substring(24, 32).toUpperCase());
    }

    /**
     * Generates a short reference code (8 chars) for display purposes
     */
    public static String generateShortReference() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    /**
     * Generates a receipt number with date component
     * Format: REC-YYYYMMDD-{UUID}
     */
    public static String generateReceiptNumber() {
        return String.format("REC-%tY%<tm%<td-%s",
                System.currentTimeMillis(),
                UUID.randomUUID().toString().substring(0, 6).toUpperCase());
    }
}
// payment.setTransactionId(ReferenceGenerator.generateTransactionId("BANK"));
//  payment.setTransactionId(ReferenceGenerator.generateTransactionId("MOB"));
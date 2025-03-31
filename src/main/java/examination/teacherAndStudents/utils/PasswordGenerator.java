package examination.teacherAndStudents.utils;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
@Component
public class PasswordGenerator {
    private static final String ALPHA_NUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom random = new SecureRandom();

    public  String generateRandomPassword() {
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            int randomIndex = random.nextInt(ALPHA_NUMERIC.length());
            sb.append(ALPHA_NUMERIC.charAt(randomIndex));
        }
        return sb.toString();
    }
}
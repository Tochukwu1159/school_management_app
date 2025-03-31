package examination.teacherAndStudents.utils;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class EncryptionUtil {

    private static final String SECRET_KEY = "1234567890abcdef";  // 16-byte key (128-bit key)

    // Encrypt the value using AES
    public static String encrypt(String value) throws Exception {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedValue = cipher.doFinal(value.getBytes());
            return Base64.getEncoder().encodeToString(encryptedValue);
        } catch (Exception e) {
            throw new Exception("Encryption failed: " + e.getMessage(), e);
        }
    }

    // Decrypt the encrypted value using AES
    public static String decrypt(String encryptedValue) throws Exception {
        try {
            // Ensure the encrypted value is a valid Base64 string
            if (encryptedValue == null || encryptedValue.isEmpty()) {
                throw new IllegalArgumentException("Encrypted value is empty or null");
            }

            byte[] decodedValue;
            try {
                decodedValue = Base64.getDecoder().decode(encryptedValue);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid Base64 string: " + encryptedValue, e);
            }

            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedValue = cipher.doFinal(decodedValue);
            return new String(decryptedValue);
        } catch (Exception e) {
            throw new Exception("Decryption failed: " + e.getMessage(), e);
        }
    }
}



package examination.teacherAndStudents.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class Encriy {

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

    public static void main(String[] args) {
        try {
            String encrypted = encrypt("tochukwu.udochukwuc@gmail.com");
            String encrypted11 = encrypt("As1234567890");
            System.out.println("Encrypted: " + encrypted);
            System.out.println("Encrypted11: " + encrypted11);

            String decrypted = decrypt(encrypted);
            String decrypted11 = decrypt(encrypted11);
            System.out.println("Decrypted11: " + decrypted11);
            System.out.println("Decrypted: " + decrypted);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

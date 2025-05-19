package iuh.paymentservice.utils;

import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class TemporaryIdGenerator {
    private static final String HMAC_SECRET = "your-hmac-secret-32-bytes-long123"; // Lưu an toàn, thay bằng key mạnh

    public String generateTemporaryId() {
        return UUID.randomUUID().toString().substring(0, 8); // 8 ký tự
    }

    public String generateSignature(String orderId, String tempId) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(HMAC_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            String data = orderId + ":" + tempId;
            byte[] hmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().encodeToString(hmac).substring(0, 16);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate signature", e);
        }
    }

    public boolean verifySignature(String orderId, String tempId, String signature) {
        String expected = generateSignature(orderId, tempId);
        return expected.equals(signature);
    }
}
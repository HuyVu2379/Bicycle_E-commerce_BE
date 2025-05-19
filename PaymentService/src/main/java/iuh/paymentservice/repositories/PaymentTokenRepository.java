package iuh.paymentservice.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
public class PaymentTokenRepository {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String TOKEN_KEY_PREFIX = "payment:jwt:";
    private static final long DEFAULT_TTL = 3600000; // 1 giờ

    @Autowired
    public PaymentTokenRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveToken(String key, String token) {
        String redisKey = TOKEN_KEY_PREFIX + key;
        redisTemplate.opsForValue().set(redisKey, token, DEFAULT_TTL, TimeUnit.MILLISECONDS);
    }

    public String getToken(String key) {
        String redisKey = TOKEN_KEY_PREFIX + key;
        return redisTemplate.opsForValue().get(redisKey);
    }

    public void deleteToken(String key) {
        String redisKey = TOKEN_KEY_PREFIX + key;
        redisTemplate.delete(redisKey);
    }
}
package iuh.userservice.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
public class TokenRepository {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String TOKEN_KEY_PREFIX = "jwt:";
    private static final String BLACKLIST_KEY_PREFIX = "blacklist:jwt:";

    @Autowired
    public TokenRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveToken(String userId, String token, long ttl) {
        String key = TOKEN_KEY_PREFIX + userId;
        System.out.println("Saving token to Redis: " + key);
        redisTemplate.opsForValue().set(key, token, ttl, TimeUnit.MILLISECONDS);
    }

    public String getToken(String userId) {
        String key = TOKEN_KEY_PREFIX + userId;
        System.out.println("check key: " + key);
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteToken(String userId) {
        String key = TOKEN_KEY_PREFIX + userId;
        redisTemplate.delete(key);
    }

    public void blacklistToken(String token, long ttl) {
        String key = BLACKLIST_KEY_PREFIX + token;
        redisTemplate.opsForValue().set(key, "blacklisted", ttl, TimeUnit.MILLISECONDS);
    }

    public boolean isTokenBlacklisted(String token) {
        String key = BLACKLIST_KEY_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
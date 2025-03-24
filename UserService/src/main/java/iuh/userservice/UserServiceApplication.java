package iuh.userservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@SpringBootApplication
@EnableDiscoveryClient
public class UserServiceApplication {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Bean
    CommandLineRunner testDatabaseConnection(DataSource dataSource) {
        return args -> {
            try (Connection conn = dataSource.getConnection()) {
                System.out.println("✅ Kết nối thành công đến database: " + conn.getCatalog());
                redisTemplate.opsForValue().set("test_key", "Hello Redis");
                System.out.println("Redis test: " + redisTemplate.opsForValue().get("test_key"));
            } catch (SQLException e) {
                System.err.println("❌ Lỗi kết nối database: " + e.getMessage());
            }
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

}

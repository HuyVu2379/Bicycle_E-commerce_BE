package iuh.gatewayservice.filters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.gatewayservice.responses.AuthResponse;
import iuh.gatewayservice.utils.JWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private JWTUtil jwtUtil;
    private static final Logger logger = LoggerFactory.getLogger(JWTUtil.class);
    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
            "/api/v1/auth/login",
            "/api/v1/users/register",
            "/api/v1/auth/refresh-token",
            "/api/v1/auth/validate-token",
            "/api/v1/address/create"
    );
    public AuthenticationFilter() {
        super(Config.class);
    }
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().toString();
            logger.info("Request path: {}", path); // Log đường dẫn request
            if (PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith)) {
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            logger.info("Authorization header: {}", authHeader); // Log header đầy đủ
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return unauthorizedResponse(exchange, "Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7).trim();
            logger.info("Extracted JWT token: {}", token); // Log token đã trích xuất

            return jwtUtil.validateToken(token)
                    .flatMap(isValid -> {
                        if (isValid) {
                            String email = jwtUtil.extractEmail(token);
                            String role = jwtUtil.extractRole(token);
                            String userId = jwtUtil.extractUserId(token);
                            ServerWebExchange modifiedExchange = exchange.mutate()
                                    .request(exchange.getRequest().mutate()
                                            .header("X-Auth-User", email)
                                            .header("X-Auth-Role",role)
                                            .header("X-Auth-UserId", userId)
                                            .build())
                                    .build();
                            return chain.filter(modifiedExchange);
                        } else {
                            return unauthorizedResponse(exchange, "Invalid token");
                        }
                    })
                    .onErrorResume(e -> unauthorizedResponse(exchange, "Token validation failed: " + e.getMessage()));
        };
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Tạo đối tượng phản hồi
        AuthResponse response = new AuthResponse(HttpStatus.UNAUTHORIZED.value(), message, false, System.currentTimeMillis());

        try {
            // Sử dụng ObjectMapper để chuyển đổi đối tượng thành JSON
            ObjectMapper objectMapper = new ObjectMapper();
            byte[] responseBytes = objectMapper.writeValueAsBytes(response);

            return exchange.getResponse()
                    .writeWith(Mono.just(exchange.getResponse()
                            .bufferFactory()
                            .wrap(responseBytes)));
        } catch (JsonProcessingException e) {
            logger.error("Error converting response to JSON", e);

            // Fallback nếu có lỗi
            String fallbackJson = "{\"status\":401,\"message\":\"Unauthorized\",\"success\":false}";
            return exchange.getResponse()
                    .writeWith(Mono.just(exchange.getResponse()
                            .bufferFactory()
                            .wrap(fallbackJson.getBytes(StandardCharsets.UTF_8))));
        }
    }
    public static class Config {
    }
}
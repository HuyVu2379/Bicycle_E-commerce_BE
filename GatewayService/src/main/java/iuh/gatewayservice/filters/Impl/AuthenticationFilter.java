package iuh.gatewayservice.filters.Impl;

import iuh.gatewayservice.restClient.AuthenticationService;
import org.apache.http.HttpHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
    public AuthenticationFilter(AuthenticationService authenticationService) {
        super(Config.class);
        this.authenticationService = authenticationService;
    }
    private final AuthenticationService authenticationService;

    private final List<String> publicEndpoints = Arrays.asList(
            "/api/v1/users/auth/login",
            "/api/v1/users/auth/register",
            "/api/v1/users/auth/refresh-token"
    );

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().toString();
            System.out.println("Request path: " + path);
            if (publicEndpoints.stream().anyMatch(path::startsWith)) {
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return unauthorizedResponse(exchange, "Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);
            return authenticationService.validateToken(token)
                    .flatMap(isValid -> {
                        if (isValid) {
                            return chain.filter(exchange);
                        } else {
                            return unauthorizedResponse(exchange, "Invalid token");
                        }
                    })
                    .onErrorResume(e -> unauthorizedResponse(exchange, "Token validation failed: " + e.getMessage()));
        };
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
        // Configuration properties can be added here if needed
    }
}
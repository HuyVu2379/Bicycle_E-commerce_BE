package iuh.gatewayservice.restClient;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AuthenticationService {

    private final UserServiceClient userServiceClient;

    public AuthenticationService(@Lazy UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    public Mono<Boolean> validateToken(String token) {
        return Mono.fromCallable(() -> userServiceClient.validateToken(token))
                .map(response -> response.getBody() != null);
    }
}
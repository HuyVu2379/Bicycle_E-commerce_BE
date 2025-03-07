package iuh.gatewayservice.restClient;

import iuh.gatewayservice.dtos.responses.MessageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service")
public interface UserServiceClient {
    @GetMapping("/auth/validate-token")
    ResponseEntity<MessageResponse<Boolean>> validateToken(@RequestParam("token") String token);
}

package iuh.productservice.client;

import iuh.productservice.dtos.requests.AddressRequest;
import iuh.productservice.dtos.requests.AuthRequest;
import iuh.productservice.dtos.responses.AddressResponse;
import iuh.productservice.dtos.responses.AuthResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name= "user-service")
public interface AuthServiceClient {
    @PostMapping("/api/v1/users/auth/login")
    AuthResponse login(@RequestBody AuthRequest authRequest);
    @PostMapping("/api/v1/users/auth/refresh-token")
    AuthResponse refreshToken(@RequestBody AuthRequest authRequest);
    @GetMapping("/api/v1/auth/validate-token")
    Boolean validateToken(@RequestParam String token);
    @PostMapping("/api/v1/address/create")
    AddressResponse createAddress(@RequestBody AddressRequest addressRequest);
}

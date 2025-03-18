package iuh.orderservice.clients;


import iuh.orderservice.dtos.requests.AddressRequest;
import iuh.orderservice.dtos.requests.AuthRequest;
import iuh.orderservice.dtos.responses.AddressResponse;
import iuh.orderservice.dtos.responses.AuthResponse;
import iuh.orderservice.dtos.responses.MessageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name= "user-service")
public interface UserServiceClient {
    @PostMapping("/api/v1/users/auth/login")
    AuthResponse login(@RequestBody AuthRequest authRequest);
    @PostMapping("/api/v1/users/auth/refresh-token")
    AuthResponse refreshToken(@RequestBody AuthRequest authRequest);
    @GetMapping("/api/v1/auth/validate-token")
    Boolean validateToken(@RequestParam String token);
    @PostMapping("/api/v1/address/create")
    MessageResponse<AddressResponse> createAddress(@RequestBody AddressRequest addressRequest);
    @GetMapping("/api/v1/address/{userId}")
    MessageResponse<AddressResponse> getAddressByUserId(@PathVariable("userId") String userId);

}

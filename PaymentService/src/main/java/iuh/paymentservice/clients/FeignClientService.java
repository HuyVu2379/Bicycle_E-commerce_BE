package iuh.paymentservice.clients;

import iuh.paymentservice.dtos.requests.OrderRequest;
import iuh.paymentservice.dtos.responses.AddressResponse;
import iuh.paymentservice.dtos.responses.OrderResponse;
import iuh.paymentservice.exception.MessageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "api-gateway", contextId = "paymentServiceClient")
public interface FeignClientService {
    @PutMapping("/api/v1/orders/update")
    MessageResponse<Integer> updateOrder(@RequestBody OrderRequest orderRequest, @RequestHeader("Authorization") String token);

    @GetMapping("/api/v1/users/getEmailUser")
    MessageResponse<String> getEmailUser(@RequestParam("userId") String userId, @RequestHeader("Authorization") String token);

    @GetMapping("/api/v1/orders/get/{orderId}")
    MessageResponse<OrderResponse> getOrderById(@PathVariable("orderId") String orderId, @RequestHeader("Authorization") String token);

    @GetMapping("/api/v1/products/public/get-name/{productId}")
    MessageResponse<String> getProductName(@PathVariable("productId") String productId);

    @GetMapping("/api/v1/address/{userId}")
    MessageResponse<AddressResponse> getAddressByUserId(@PathVariable("userId") String userId, @RequestHeader("Authorization") String token);
}

package iuh.paymentservice.clients;

import iuh.paymentservice.dtos.requests.OrderRequest;
import iuh.paymentservice.exception.MessageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "api-gateway", contextId = "paymentServiceClient")
public interface FeignClientService {
    @PutMapping("/api/v1/orders/update")
    MessageResponse<Integer> updateOrder(@RequestBody OrderRequest orderRequest, @RequestHeader("Authorization") String token);
}

package iuh.productservice.client;

import iuh.productservice.dtos.responses.PromotionResponse;
import iuh.productservice.dtos.responses.MessageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "order-service")
public interface OrderServiceClient {
    @GetMapping("/api/v1/promotions/{id}")
    MessageResponse<PromotionResponse> getPromotion(@RequestHeader("Authorization") String accessToken, @PathVariable String id);
}

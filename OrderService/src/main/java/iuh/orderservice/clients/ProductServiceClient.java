package iuh.orderservice.clients;

import iuh.orderservice.dtos.responses.PriceRespone;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service")
public interface ProductServiceClient {
    @GetMapping("/api/v1/products/public/get-price/{productId}")
    PriceRespone getPrice(@PathVariable String productId);
}

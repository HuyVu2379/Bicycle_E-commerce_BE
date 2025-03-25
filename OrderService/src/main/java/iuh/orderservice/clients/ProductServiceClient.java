package iuh.orderservice.clients;

import iuh.orderservice.dtos.responses.ProductNameRespone;
import iuh.orderservice.dtos.responses.ProductPriceRespone;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service")
public interface ProductServiceClient {
    @GetMapping("/api/v1/products/public/get-price/{productId}")
    ProductPriceRespone getPrice(@PathVariable String productId);

    @GetMapping("/api/v1/products/public/get-name/{productId}")
    ProductNameRespone getName(@PathVariable String productId);
}

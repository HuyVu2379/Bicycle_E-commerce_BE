package iuh.orderservice.clients;

import iuh.orderservice.dtos.responses.MessageResponse;
import iuh.orderservice.dtos.responses.ProductNameRespone;
import iuh.orderservice.dtos.responses.ProductPriceRespone;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "api-gateway")
public interface ProductServiceClient {
    @GetMapping("/api/v1/products/public/get-price/{productId}")
    ProductPriceRespone getPrice(@PathVariable String productId);

    @GetMapping("/api/v1/products/public/get-name/{productId}")
    ProductNameRespone getName(@PathVariable String productId);

    @PostMapping("/api/v1/inventories/public/reduce-quantity/{productId}/{color}/{quantity}")
    ResponseEntity<MessageResponse<Object>> reduceInventory(
            @PathVariable("productId") String productId,
            @PathVariable("color") String color,
            @PathVariable("quantity") int quantity
    );
}

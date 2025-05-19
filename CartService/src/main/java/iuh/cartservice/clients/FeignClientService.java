package iuh.cartservice.clients;

import iuh.cartservice.dtos.requests.Inventory;
import iuh.cartservice.dtos.responses.MessageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "api-gateway", contextId = "cartServiceClient")
public interface FeignClientService {
    @GetMapping("/api/v1/inventories/public/getAllInventoryByProductId/{productId}")
    MessageResponse<List<Inventory>> getInventoryByProductId(@PathVariable("productId") String productId, @RequestHeader("Authorization") String token);
    @GetMapping("/api/v1/products/public/get-name/{productId}")
    MessageResponse<String> getProductName(@PathVariable String productId, @RequestHeader("Authorization") String token);
    @GetMapping("/api/v1/products/public/get-price/{productId}")
    MessageResponse<Double> getProductPrice(@PathVariable String productId, @RequestHeader("Authorization") String token);
}

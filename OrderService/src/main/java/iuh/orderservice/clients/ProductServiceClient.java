package iuh.orderservice.clients;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "product-service")
public interface ProductServiceClient {

}

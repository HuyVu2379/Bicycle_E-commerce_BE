package iuh.productservice.repositories.restClient;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "user-service", contextId = "addressClient")
public interface addressClient {
}

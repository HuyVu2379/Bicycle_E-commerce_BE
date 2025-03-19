package iuh.orderservice.controllers;

import iuh.orderservice.dtos.requests.CreateOrderRequest;
import iuh.orderservice.dtos.responses.MessageResponse;
import iuh.orderservice.dtos.responses.OrderDetailResponse;
import iuh.orderservice.dtos.responses.OrderResponse;
import iuh.orderservice.dtos.responses.SuccessEntityResponse;
import iuh.orderservice.entities.Order;
import iuh.orderservice.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<MessageResponse<Object>> createOrder(@RequestBody CreateOrderRequest request){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        Optional<Order> orderOpt = orderService.createOrder(request, userId);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new MessageResponse<>(400,
                            "Order creation failed",
                            false,
                            null
                    ));
        }
        return SuccessEntityResponse.created("Order create sucessfully", orderOpt.get());
    }
}

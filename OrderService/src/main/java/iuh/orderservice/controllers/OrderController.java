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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
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

    @PostMapping("/delete/{orderId}")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<MessageResponse<Object>> deleteOrder(@PathVariable String orderId){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        boolean isDeleted = orderService.deleteOrder(orderId, userId);
        if (!isDeleted) {
            return ResponseEntity.badRequest().body(
                    new MessageResponse<>(400,
                            "Order deletion failed",
                            false,
                            null
                    ));
        }
        return SuccessEntityResponse.ok("Order deleted", isDeleted);
    }

    @GetMapping("/get/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<MessageResponse<Object>> getOrder(@PathVariable String orderId){
        Optional<Order> orderOpt = orderService.getOrderById(orderId);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new MessageResponse<>(400,
                            "Order not found",
                            false,
                            null
                    ));
        }
        return SuccessEntityResponse.found("Order found", orderOpt.get());
    }

    @GetMapping("/get-by-user")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<MessageResponse<Object>> getOrderByUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        List<Order> orders = orderService.getOrdersByUserId(userId);
        if (orders.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new MessageResponse<>(400,
                            "Orders not found",
                            false,
                            null
                    ));
        }
        return SuccessEntityResponse.found("Orders found", orders);
    }

    @GetMapping("/get-revenue-by-time")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<MessageResponse<Object>> getRevenueByTime(@RequestParam String startTime, @RequestParam String endTime){
        double orderOpt = orderService.getRevenueByTime(startTime, endTime);
        if (orderOpt == 0) {
            return ResponseEntity.badRequest().body(
                    new MessageResponse<>(400,
                            "Revenue not found",
                            false,
                            null
                    ));
        }
        return SuccessEntityResponse.ok("Revenue found", orderOpt);
    }

    @GetMapping("/get-revenue-by-year/{year}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<MessageResponse<Object>> getRevenueByYear(@PathVariable String year){
        Map<String, Double> revenues = orderService.getRevenueByYear(Integer.parseInt(year));
        if (revenues.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new MessageResponse<>(400,
                            "Revenue not found",
                            false,
                            null
                    ));
        }
        return SuccessEntityResponse.ok("Revenue found", revenues);
    }

    @GetMapping("/get-revenue-by-users")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<MessageResponse<Object>> getRevenueByUsers(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "totalRevenue") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection){
        List<Map<String, Object>> revenues = orderService.getRevenueByUsers(pageNo, pageSize, sortBy, sortDirection).stream().collect(Collectors.toList());
        if (revenues.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new MessageResponse<>(400,
                            "Revenue not found",
                            false,
                            null
                    ));
        }
        return SuccessEntityResponse.ok("Revenue found", revenues);
    }

}

package iuh.orderservice.controllers;

import iuh.orderservice.dtos.requests.CreateOrderRequest;
import iuh.orderservice.dtos.requests.OrderRequest;
import iuh.orderservice.dtos.responses.MessageResponse;
import iuh.orderservice.dtos.responses.OrderDetailResponse;
import iuh.orderservice.dtos.responses.OrderResponse;
import iuh.orderservice.dtos.responses.SuccessEntityResponse;
import iuh.orderservice.entities.Order;
import iuh.orderservice.services.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private HttpServletRequest httpServletRequest;

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<MessageResponse<Object>> createOrder(@RequestBody CreateOrderRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        String token = httpServletRequest.getHeader("Authorization");
        Optional<Order> orderOpt = orderService.createOrder(request, userId, token);
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

    @PutMapping("/update")
    public ResponseEntity<MessageResponse<Integer>> updateOrder(@RequestBody OrderRequest orderRequest) {
        int result = orderService.updateOrder(orderRequest.getOrderId(), orderRequest.getOrderStatus());
        if (result == 0) {
            return ResponseEntity.badRequest().body(
                    new MessageResponse<>(400,
                            "Order update failed",
                            false,
                            null
                    ));
        }
        return SuccessEntityResponse.ok("Order updated", result);
    }

    @PostMapping("/delete/{orderId}")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<MessageResponse<Object>> deleteOrder(@PathVariable String orderId) {
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

    @GetMapping("/get-all")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<MessageResponse<Object>> getAllOrders(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        Page<Order> orders = orderService.getAllOrders(pageNo, pageSize, sortBy, sortDirection);
        if (orders.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new MessageResponse<>(400,
                            "Orders not found",
                            false,
                            null
                    ));
        }
        return SuccessEntityResponse.ok("Orders found", orders);
    }

    @GetMapping("/get-by-user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN')" + " || hasAnyRole('USER')")
    public ResponseEntity<MessageResponse<Object>> getOrdersByUserId(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        Page<Order> orders = orderService.getOrdersPageByUserId(pageNo, pageSize, sortBy, sortDirection, userId);
        if (orders.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new MessageResponse<>(400,
                            "Orders not found",
                            false,
                            null
                    ));
        }
        return SuccessEntityResponse.ok("Orders found", orders);
    }

    @GetMapping("/get/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN')" + " || hasAnyRole('USER')")
    public ResponseEntity<MessageResponse<Object>> getOrder(@PathVariable String orderId) {
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

    @GetMapping("/history-orders")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<MessageResponse<Object>> getHistoryOrdersByUser(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        Page<Order> orders = orderService.getOrdersPageByUserId(pageNo, pageSize, sortBy, sortDirection, userId);
        if (orders == null) {
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
    public ResponseEntity<MessageResponse<Object>> getRevenueByTime(@RequestParam String startTime, @RequestParam String endTime) {
        BigDecimal orderOpt = orderService.getRevenueByTime(startTime, endTime);
        if (orderOpt == null) {
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
    public ResponseEntity<MessageResponse<Object>> getRevenueByYear(@PathVariable String year) {
        Map<String, BigDecimal> revenues = orderService.getRevenueByYear(Integer.parseInt(year));
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
            @RequestParam(defaultValue = "desc") String sortDirection) {
        Page<Map<String, Object>> revenues = orderService.getRevenueByUsers(
                pageNo,
                pageSize,
                sortBy,
                sortDirection
        );
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

package iuh.orderservice.controllers;

import iuh.orderservice.dtos.responses.MessageResponse;
import iuh.orderservice.dtos.responses.SuccessEntityResponse;
import iuh.orderservice.entities.OrderDetail;
import iuh.orderservice.services.OrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(("/api/v1/order-details"))
public class OrderDetailController {
    @Autowired
    private OrderDetailService orderDetailService;

    @GetMapping("/get/{orderId}")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<MessageResponse<Object>> getOrderDetailsByOrderId(@PathVariable String orderId){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        List<OrderDetail> orderDetails = orderDetailService.getOrderDetailsByOrderId(orderId, userId);
        if (orderDetails.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new MessageResponse<>(400,
                            "Get order details failed",
                            false,
                            null
                    ));
        }
        return SuccessEntityResponse.found("Get order details successfully", orderDetails);
    }

    @GetMapping("/get-revenue-by-products")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<MessageResponse<Object>> getRevenueByProducts(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "totalRevenue") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection){
        Page<Map<String, Object>> revenueByProducts = orderDetailService
                .getRevenueByProducts(pageNo, pageSize, sortBy, sortDirection);
        if (revenueByProducts.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new MessageResponse<>(400,
                            "Get revenue by products failed",
                            false,
                            null
                    ));
        }
        return SuccessEntityResponse.ok("Get revenue by products successfully", revenueByProducts);
    }

}

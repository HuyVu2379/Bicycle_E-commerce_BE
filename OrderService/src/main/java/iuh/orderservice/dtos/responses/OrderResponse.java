package iuh.orderservice.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private String orderId;
    private String userId;
    private LocalDateTime orderDate;
    private double totalPrice;
    private String promotionId;
    private List<OrderDetailResponse> orderDetails;
}

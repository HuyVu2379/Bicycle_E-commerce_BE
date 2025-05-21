package iuh.paymentservice.dtos.responses;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import iuh.paymentservice.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "orderId")
public class OrderResponse {
    private String orderId;
    private String userId;
    private LocalDateTime orderDate;
    private double totalPrice;
    private String shippingAddress;
    private OrderStatus status = OrderStatus.PENDING;
    private List<OrderDetailResponse> orderDetails;
    private String promotionId = null;
}

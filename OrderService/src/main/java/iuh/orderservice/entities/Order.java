package iuh.orderservice.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import iuh.orderservice.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(of = {"orderId"})
@Table(name = "orders")
public class Order extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String orderId;
    @Column(nullable = false)
    private String userId;
    private LocalDateTime orderDate;
    private double totalPrice;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;
    @OneToMany(mappedBy = "order",cascade = CascadeType.ALL,orphanRemoval = true)
    @JsonManagedReference
    private List<OrderDetail> orderDetails;
    @Column(nullable = true)
    private String promotionId = null;
}

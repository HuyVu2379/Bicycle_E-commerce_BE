package iuh.orderservice.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(of = {"orderId"})
public class Order extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String orderId;
    @Column(nullable = false)
    private String userId;
    private LocalDateTime orderDate;
    private double totalPrice;
    @OneToMany(mappedBy = "order",cascade = CascadeType.ALL,orphanRemoval = true)
    @JsonManagedReference
    private List<OrderDetail> orderDetails;
    @Column(nullable = true)
    private String promotionId = null;
}

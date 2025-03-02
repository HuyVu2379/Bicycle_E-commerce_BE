package iuh.orderservice.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Entity
@Data
@EqualsAndHashCode(of = {"orderDetailId"})
public class OrderDetail extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String orderDetailId;
    @Column(nullable = false)
    private String productId;
    @Column(nullable = false)
    private int quantity;
    @Column(nullable = false)
    private double subtotal = 0;
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
    public double calcSubtotal(int quantity,double price){
        return quantity*price;
    }
}

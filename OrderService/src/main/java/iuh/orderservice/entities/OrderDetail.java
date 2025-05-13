package iuh.orderservice.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @Column(nullable = true)
    private String color;
    @Column(nullable = false)
    private int quantity;
    @Column(nullable = false)
    private double subtotal = 0;
    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonIgnore // to prevent infinite loop when serializing
    private Order order;
    public double calcSubtotal(int quantity,double price){
        return quantity*price;
    }
}

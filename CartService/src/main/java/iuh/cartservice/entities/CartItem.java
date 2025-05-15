package iuh.cartservice.entities;


import com.fasterxml.jackson.annotation.JsonBackReference;
import iuh.cartservice.enums.Color;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cart_items")
@Data
@EqualsAndHashCode(of = {"cartItemId"})
public class CartItem extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String cartItemId;
    private String productId;
    private Color color;
    private int quantity;
    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    @JsonBackReference
    private Cart cart;
}

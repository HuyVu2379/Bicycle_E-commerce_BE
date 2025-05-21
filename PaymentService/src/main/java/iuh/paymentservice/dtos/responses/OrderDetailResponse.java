package iuh.paymentservice.dtos.responses;


import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "orderDetailId")
public class OrderDetailResponse {
    private String orderDetailId;
    private String productId;
    private String color;
    private int quantity;
    private double subtotal = 0;
    public double calcSubtotal(int quantity, double price) {
        return quantity * price;
    }
}

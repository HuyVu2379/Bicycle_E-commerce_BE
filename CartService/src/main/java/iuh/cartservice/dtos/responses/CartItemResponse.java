package iuh.cartservice.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class CartItemResponse {
    private String cartId;
    private String cartItemId;
    private String productId;
    private String productName;
    private double price;
    private String imageUrl;
    private String color;
    private int quantity;
}

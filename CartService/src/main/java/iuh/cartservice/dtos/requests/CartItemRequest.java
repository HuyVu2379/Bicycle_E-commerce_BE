package iuh.cartservice.dtos.requests;

import iuh.cartservice.enums.Color;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class CartItemRequest {
    private String cartId;
    private String productId;
    private String color;
    private int quantity;
}

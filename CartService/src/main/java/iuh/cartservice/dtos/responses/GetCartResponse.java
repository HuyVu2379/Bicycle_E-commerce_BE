package iuh.cartservice.dtos.responses;

import lombok.Data;

import java.util.List;

@Data
public class GetCartResponse {
    private String cartId;
    private String userId;
    private List<CartItemResponse> items;
}

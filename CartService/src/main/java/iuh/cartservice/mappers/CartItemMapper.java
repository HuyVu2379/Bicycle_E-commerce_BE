package iuh.cartservice.mappers;
import iuh.cartservice.dtos.requests.CartItemRequest;
import iuh.cartservice.entities.CartItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CartItemMapper {
    CartItem CartItemRequestToCartItem(CartItemRequest cartItemRequest);
}

package iuh.cartservice.mappers;

import iuh.cartservice.dtos.requests.CartRequest;
import iuh.cartservice.dtos.responses.CartResponse;
import iuh.cartservice.entities.Cart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartMapper {
    @Mapping(source = "userId", target = "userId")
    Cart CartRequestToCart(CartRequest cartRequest);

    CartResponse CartToCartResponse(Cart cart);
}

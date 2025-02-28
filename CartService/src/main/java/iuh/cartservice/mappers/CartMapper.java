package iuh.cartservice.mappers;

import iuh.cartservice.dtos.requests.CartRequest;
import iuh.cartservice.dtos.responses.CartResponse;
import iuh.cartservice.entities.Cart;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CartMapper {
    CartMapper INSTANCE = Mappers.getMapper(CartMapper.class);

    Cart CartRequestToCart(CartRequest cartRequest);

    CartResponse CartToCartResponse(Cart cart);
}

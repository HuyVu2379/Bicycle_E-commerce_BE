package iuh.cartservice.controllers;

import iuh.cartservice.dtos.requests.CartRequest;
import iuh.cartservice.entities.Cart;
import iuh.cartservice.exception.MessageResponse;
import iuh.cartservice.mappers.CartMapper;
import iuh.cartservice.services.Impl.CartServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/carts")
public class CartController {
    @Autowired
    private CartServiceImpl cartService;
    @Autowired
    private CartMapper cartMapper;


    @PostMapping(value = "/create", produces = "application/json")
    public MessageResponse<Cart> createCart(@RequestBody CartRequest cartRequest) {
        Cart cart = cartMapper.CartRequestToCart(cartRequest);
        cartService.createCart(cart);
        return new MessageResponse<>(200, "Cart created", true, cart);
    }
}

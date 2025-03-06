package iuh.cartservice.controllers;

import iuh.cartservice.dtos.requests.CartRequest;
import iuh.cartservice.dtos.responses.MessageResponse;
import iuh.cartservice.dtos.responses.SuccessEntityResponse;
import iuh.cartservice.entities.Cart;
import iuh.cartservice.entities.CartItem;
import iuh.cartservice.exception.errors.CartNotFoundException;
import iuh.cartservice.mappers.CartMapper;
import iuh.cartservice.services.CartItemService;
import iuh.cartservice.services.Impl.CartServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/carts")
public class CartController {
    @Autowired
    private CartServiceImpl cartService;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private CartItemService cartItemService;

    @PostMapping(value = "/create", produces = "application/json")
    public ResponseEntity<MessageResponse<Cart>> createCart(@RequestBody CartRequest cartRequest) {
        Cart cart = cartMapper.CartRequestToCart(cartRequest);
       try {
           Optional<Cart> result = cartService.createCart(cart);
           if(result.isPresent()){
               return SuccessEntityResponse.created("Cart created successfully", result.get());
           }
           else{
               throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Cart creation failed");
           }
       }catch (Exception e){
           throw e;
       }
    }
    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<MessageResponse<Cart>> getCartById(@PathVariable String id) {
        System.out.println("id: " + id);
        Optional<Cart> result = cartService.getCartById(id);
        if(result.isPresent()){
            return SuccessEntityResponse.found("Found cart", result.get());
        } else {
            throw new CartNotFoundException("Cart not found with ID: " + id);
        }
    }

    @PostMapping(value = "/remove-cart-item/{cartItemId}", produces = "application/json")
    public ResponseEntity<MessageResponse<Boolean>> removeCartItem(@PathVariable String cartItemId) {
        try {
            boolean result = cartItemService.removeCartItem(cartItemId);
            if(result){
                return SuccessEntityResponse.created("CartItem removed successfully", result);
            }
            else{
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "CartItem removing failed");
            }
        } catch (Exception e) {
            throw e;
        }
    }

    @PostMapping(value = "/create-cart-item", produces = "application/json")
    public ResponseEntity<MessageResponse<CartItem>> createCartItem(@RequestBody CartItem cartItem) {
        try {
            System.out.println("cartItem: " + cartItem);
            Optional<CartItem> result = cartItemService.addCartItem(cartItem);
            if(result.isPresent()){
                return SuccessEntityResponse.created("CartItem created successfully", result.get());
            }
            else{
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "CartItem creation failed");
            }
        } catch (Exception e) {
            throw e;
        }
    }
}

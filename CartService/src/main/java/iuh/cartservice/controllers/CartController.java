package iuh.cartservice.controllers;

import iuh.cartservice.dtos.requests.CartRequest;
import iuh.cartservice.dtos.responses.MessageResponse;
import iuh.cartservice.dtos.responses.SuccessEntityResponse;
import iuh.cartservice.entities.Cart;
import iuh.cartservice.entities.CartItem;
import iuh.cartservice.exception.errors.CartNotFoundException;
import iuh.cartservice.mappers.CartMapper;
import iuh.cartservice.repositories.CartItemRepository;
import iuh.cartservice.services.CartItemService;
import iuh.cartservice.services.Impl.CartServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    @Autowired
    private CartItemRepository cartItemRepository;

    @GetMapping(value = "/all", produces = "application/json")
    public ResponseEntity<MessageResponse<Iterable<Cart>>> getAllCarts() {
        try {
            Iterable<Cart> result = cartService.getAllCarts();
            if (result.iterator().hasNext()) {
                return SuccessEntityResponse.found("All carts fetched successfully", result);
            } else {
                throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch carts");
            }
        } catch (Exception e) {
            throw e;
        }
    }
    @PreAuthorize("hasRole('USER')")
    @PostMapping(value = "/create", produces = "application/json")
    public ResponseEntity<MessageResponse<Cart>> createCart() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) authentication.getDetails();
        CartRequest cartRequest = new CartRequest();
        cartRequest.setUserId(userId);
        Cart cart = cartMapper.CartRequestToCart(cartRequest);
        try {
            Optional<Cart> result = cartService.createCart(cart);
            if (result.isPresent()) {
                return SuccessEntityResponse.created("Cart created successfully", result.get());
            } else {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Cart creation failed");
            }
        } catch (Exception e) {
            throw e;
        }
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<MessageResponse<Cart>> getCartById(@PathVariable String id) {
        Optional<Cart> result = cartService.getCartById(id);
        try{
            if (result.isPresent()) {
                return SuccessEntityResponse.found("Found cart", result.get());
            }
        }catch (Exception e){
            throw e;
        }
         return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse<>(HttpStatus.NOT_FOUND.value(), "Cart not found", false, null));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(value = "/find-cart-by-userId/{id}", produces = "application/json")
    public ResponseEntity<MessageResponse<Cart>> getCartByUserId(@PathVariable String id) {
        Optional<Cart> result = cartService.getCartByUserId(id);
        try{
            if (result.isPresent()) {
                return SuccessEntityResponse.found("Found cart", result.get());
            }
        }catch (Exception e){
            throw e;
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse<>(HttpStatus.NOT_FOUND.value(), "Cart not found", false, null));
    }
}

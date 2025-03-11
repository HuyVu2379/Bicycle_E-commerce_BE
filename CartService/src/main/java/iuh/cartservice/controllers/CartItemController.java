package iuh.cartservice.controllers;

import iuh.cartservice.dtos.requests.BulkDeleteCartItemRequest;
import iuh.cartservice.dtos.requests.CartItemRequest;
import iuh.cartservice.dtos.responses.MessageResponse;
import iuh.cartservice.dtos.responses.SuccessEntityResponse;
import iuh.cartservice.entities.Cart;
import iuh.cartservice.entities.CartItem;
import iuh.cartservice.exception.errors.CartNotFoundException;
import iuh.cartservice.mappers.CartItemMapper;
import iuh.cartservice.services.Impl.CartItemServiceImpl;
import iuh.cartservice.services.Impl.CartServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/cart-items")
public class CartItemController {
    private final CartItemServiceImpl cartItemService;
    private final CartServiceImpl cartService;
    private final CartItemMapper cartItemMapper;

    @Autowired
    public CartItemController(CartItemServiceImpl cartItemService, CartServiceImpl cartService, CartItemMapper cartItemMapper) {
        this.cartItemService = cartItemService;
        this.cartService = cartService;
        this.cartItemMapper = cartItemMapper;
    }

    //OK
    @PreAuthorize("hasRole('USER')")
    @GetMapping(value = "/all", produces = "application/json")
    public ResponseEntity<MessageResponse<List<CartItem>>> getAllCartItems() {
        try{
            return SuccessEntityResponse.found("Get all cartItem succeed",cartItemService.getAllCartItems()) ;
        } catch (Exception e) {
            throw e;
        }
    }
    //OK
    @PreAuthorize("hasRole('USER')")
    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<CartItem> getCartItemById(@PathVariable String id) {
        Optional<CartItem> result = cartItemService.getCartItemsById(id);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    //OK
    @PreAuthorize("hasRole('USER')")
    @PostMapping(value = "/remove/{cartItemId}", produces = "application/json")
    public ResponseEntity<MessageResponse<Boolean>> removeCartItem(@PathVariable String cartItemId) {
        try {
            boolean result = cartItemService.removeCartItem(cartItemId);
            if (result) {
                return SuccessEntityResponse.created("CartItem removed successfully", result);
            } else {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "CartItem removing failed");
            }
        } catch (Exception e) {
            throw e;
        }
    }
    //OK
    @PreAuthorize("hasRole('USER')")
    @PostMapping(value = "/create", produces = "application/json")
    public ResponseEntity<MessageResponse<CartItem>> createCartItem(@RequestBody CartItemRequest cartItemRequest) {
        try {
            Cart cart = cartService.getCartById(cartItemRequest.getCartId()).orElse(null);
            if (cart == null) {
                throw new CartNotFoundException("Cart not found");
            }
            CartItem cartItem = cartItemMapper.CartItemRequestToCartItem(cartItemRequest);
            cartItem.setCart(cart);
            Optional<CartItem> result = cartItemService.addCartItem(cartItem);
            if (result.isPresent()) {
                return SuccessEntityResponse.created("CartItem created successfully", result.get());
            } else {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "CartItem creation failed");
            }
        } catch (Exception e) {
            throw e;
        }
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping(value = "/update-quantity/{cartItemId}", produces = "application/json")
    public ResponseEntity<MessageResponse<CartItem>> updateCartItemQuantity(@PathVariable String cartItemId, @RequestBody Integer quantity) {
        try {
            System.out.println("cartItemId: " + cartItemId);
            System.out.println("quantity: " + quantity);
            Optional<CartItem> result = cartItemService.updateCartItem(cartItemId, quantity);
            if (result.isPresent()) {
                return SuccessEntityResponse.created("CartItem updated successfully", result.get());
            } else {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "CartItem update failed");
            }
        } catch (Exception e) {
            throw e;
        }
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping(value = "/delete-all", produces = "application/json")
    public ResponseEntity<MessageResponse<Boolean>> deleteAllCartItems(@RequestBody CartItemRequest cartItemRequest) {
        try {
            System.out.println("cartId: " + cartItemRequest.getCartId());
            boolean result = cartItemService.deleteAllCartItem(cartItemRequest.getCartId());
            if (result) {
                return SuccessEntityResponse.ok("All cart items deleted successfully", result);
            } else {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Cart items deletion failed");
            }
        } catch (Exception e) {
            throw e;
        }
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping(value = "/bulk-delete", produces = "application/json")
    public ResponseEntity<MessageResponse<Boolean>> bulkDeleteCartItem(@RequestBody BulkDeleteCartItemRequest request) {
        try {
            boolean result = cartItemService.bulkDelete(Arrays.asList(request.getCartItemIds()));
            if (result) {
                return SuccessEntityResponse.ok("Bulk delete cart items successfully", result);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse<>(HttpStatus.BAD_REQUEST.value(), "Bulk delete cart items failed", false, null));
        } catch (Exception e) {
            throw e;
        }
    }
}

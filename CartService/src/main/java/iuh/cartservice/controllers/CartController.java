package iuh.cartservice.controllers;

import iuh.cartservice.clients.FeignClientService;
import iuh.cartservice.dtos.requests.CartRequest;
import iuh.cartservice.dtos.requests.Inventory;
import iuh.cartservice.dtos.responses.CartItemResponse;
import iuh.cartservice.dtos.responses.GetCartResponse;
import iuh.cartservice.dtos.responses.MessageResponse;
import iuh.cartservice.dtos.responses.SuccessEntityResponse;
import iuh.cartservice.entities.Cart;
import iuh.cartservice.entities.CartItem;
import iuh.cartservice.exception.errors.CartNotFoundException;
import iuh.cartservice.mappers.CartMapper;
import iuh.cartservice.repositories.CartItemRepository;
import iuh.cartservice.services.CartItemService;
import iuh.cartservice.services.Impl.CartServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.List;
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
    @Autowired
    private HttpServletRequest httpServletRequest;
    @Autowired
    private FeignClientService feignClientService;

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
        try {
            if (result.isPresent()) {
                return SuccessEntityResponse.found("Found cart", result.get());
            }
        } catch (Exception e) {
            throw e;
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse<>(HttpStatus.NOT_FOUND.value(), "Cart not found", false, null));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(value = "/find-cart-by-userId/{id}", produces = "application/json")
    public ResponseEntity<MessageResponse<GetCartResponse>> getCartByUserId(@PathVariable String id) {
        Optional<Cart> result = cartService.getCartByUserId(id);
        String token = httpServletRequest.getHeader("Authorization");
        try {
            GetCartResponse getCartResponse = new GetCartResponse();
            getCartResponse.setUserId(id);
            getCartResponse.setCartId(result.get().getCartId());
            // set CartItemResponse
            List<CartItemResponse> cartItemResponseList = new ArrayList<>();
            List<CartItem> listCartItem = cartItemService.getAllCartItemByUserId(id);
            CartItemResponse cartItemResponse = new CartItemResponse();
            listCartItem.forEach(cartItem -> {
                cartItemResponse.setCartId(result.get().getCartId());
                cartItemResponse.setCartItemId(cartItem.getCartItemId());
                cartItemResponse.setColor(cartItem.getColor().toString());
                cartItemResponse.setQuantity(cartItem.getQuantity());
                MessageResponse<List<Inventory>> listMessageResponse = feignClientService.getInventoryByProductId(cartItem.getProductId(), token);
                cartItemResponse.setImageUrl(listMessageResponse.getData().get(0).getImageUrls().get(0));
                MessageResponse<String> productNameResponse = feignClientService.getProductName(cartItem.getProductId(), token);
                cartItemResponse.setProductName(productNameResponse.getData());
                cartItemResponse.setProductId(cartItem.getProductId());
                cartItemResponseList.add(cartItemResponse);
            });
            getCartResponse.setItems(cartItemResponseList);
            if(getCartResponse!=null){
                return SuccessEntityResponse.ok("get all cartItem successfully", getCartResponse);
            }
            else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse<>(HttpStatus.NOT_FOUND.value(), "Cart not found", false, null));
            }
        } catch (Exception e) {
            throw e;
        }

    }
}

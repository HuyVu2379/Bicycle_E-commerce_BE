package iuh.cartservice.services;

import iuh.cartservice.entities.Cart;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface CartService {
    Optional<Cart> createCart(Cart cart);
    Optional<Cart> getCartById(String id);
}

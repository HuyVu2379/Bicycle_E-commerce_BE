package iuh.cartservice.services;

import iuh.cartservice.entities.Cart;
import iuh.cartservice.entities.CartItem;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface CartService {
    public Iterable<Cart> getAllCarts();
    public Optional<Cart> createCart(Cart cart);
    public Optional<Cart> getCartById(String id);
}

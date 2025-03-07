package iuh.cartservice.services;

import iuh.cartservice.entities.CartItem;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface CartItemService {
    public Optional<CartItem> addCartItem(CartItem cartItem);
    public Optional<CartItem> updateCartItem(String id, int quantity);
    public Optional<CartItem> getCartItemById(String id);
    public boolean removeCartItem(String id);
    public boolean removeAllCartItems();
}

package iuh.cartservice.services;

import iuh.cartservice.entities.CartItem;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Optional;
import java.util.List;

@Service
public interface CartItemService {
    public Optional<CartItem> addCartItem(CartItem cartItem);

    public Optional<CartItem> updateCartItem(String id, int quantity);

    public boolean removeCartItem(String id);

    public boolean removeAllCartItems();

    public Optional<CartItem> getCartItemsById(String cartId);

    public List<CartItem> getAllCartItems();

    public boolean bulkDelete(List<String> array);
    public boolean deleteAllCartItem(String cartId);
}

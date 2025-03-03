package iuh.cartservice.services.Impl;

import iuh.cartservice.entities.CartItem;
import iuh.cartservice.repositories.CartItemRepository;
import iuh.cartservice.services.CartItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CartItemServiceImpl implements CartItemService {
    @Autowired
    private CartItemRepository cartItemRepository;

    @Override
    public Optional<CartItem> addCartItem(CartItem cartItem) {
        return Optional.of(cartItemRepository.save(cartItem));
    }

    @Override
    public Optional<CartItem> updateCartItem(String id, int quantity) {
        return Optional.empty();
    }

    @Override
    public Optional<CartItem> getCartItemById(String id) {
        return Optional.empty();
    }

    @Override
    public boolean removeCartItem(String id) {
        if(!cartItemRepository.existsById(id)) {
            return false;
        }
        else{
            cartItemRepository.deleteById(id);
            return true;
        }
    }

    @Override
    public boolean removeAllCartItems() {
        return false;
    }
}

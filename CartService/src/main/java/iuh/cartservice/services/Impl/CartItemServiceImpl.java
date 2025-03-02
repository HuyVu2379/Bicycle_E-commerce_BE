package iuh.cartservice.services.Impl;

import iuh.cartservice.entities.CartItem;
import iuh.cartservice.services.CartItemService;

import java.util.Optional;

public class CartItemServiceImpl implements CartItemService {
    @Override
    public Optional<CartItem> addCartItem(String id) {
        return Optional.empty();
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
        return false;
    }

    @Override
    public boolean removeAllCartItems() {
        return false;
    }
}

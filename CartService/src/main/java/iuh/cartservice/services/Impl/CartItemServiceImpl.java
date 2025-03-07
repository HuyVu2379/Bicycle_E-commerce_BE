<<<<<<< HEAD
package iuh.cartservice.services.Impl;
=======
/*
 * @ (#) CartItemServiceImpl.java       1.0     3/7/2025
 *
 * Copyright (c) 2025. All rights reserved.
 */

package iuh.cartservice.services.Impl;
/*
 * @author: Luong Tan Dat
 * @date: 3/7/2025
 */
>>>>>>> LuongTanDat

import iuh.cartservice.entities.CartItem;
import iuh.cartservice.repositories.CartItemRepository;
import iuh.cartservice.services.CartItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

<<<<<<< HEAD
=======
import java.util.List;
>>>>>>> LuongTanDat
import java.util.Optional;

@Service
public class CartItemServiceImpl implements CartItemService {
<<<<<<< HEAD
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
=======
    private CartItemRepository cartItemRepository;

    @Autowired
    public CartItemServiceImpl(CartItemRepository cartItemRepository) {
        this.cartItemRepository = cartItemRepository;
    }

    @Override
    public Optional<CartItem> getCartItemsById(String cartId) {
        return cartItemRepository.findById(cartId);
    }

    @Override
    public List<CartItem> getAllCartItems() {
        return cartItemRepository.findAll();
>>>>>>> LuongTanDat
    }
}

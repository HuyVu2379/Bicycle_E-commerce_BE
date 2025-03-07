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

import iuh.cartservice.entities.CartItem;
import iuh.cartservice.repositories.CartItemRepository;
import iuh.cartservice.services.CartItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartItemServiceImpl implements CartItemService {
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
    }
}

/*
 * @ (#) CartItemService.java       1.0     3/7/2025
 *
 * Copyright (c) 2025. All rights reserved.
 */

package iuh.cartservice.services;

import iuh.cartservice.entities.CartItem;

import java.util.List;
import java.util.Optional;

/*
 * @author: Luong Tan Dat
 * @date: 3/7/2025
 */
public interface CartItemService {
    Optional<CartItem> getCartItemsById(String cartId);
    List<CartItem> getAllCartItems();
}

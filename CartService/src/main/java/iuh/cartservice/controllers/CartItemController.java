/*
 * @ (#) CartItemController.java       1.0     3/7/2025
 *
 * Copyright (c) 2025. All rights reserved.
 */

package iuh.cartservice.controllers;
/*
 * @author: Luong Tan Dat
 * @date: 3/7/2025
 */

import iuh.cartservice.entities.CartItem;
import iuh.cartservice.services.CartItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/cart-items")
public class CartItemController {
    private final CartItemService cartItemService;

    @Autowired
    public CartItemController(CartItemService cartItemService) {
        this.cartItemService = cartItemService;
    }

    @GetMapping(value = "/all", produces = "application/json")
    public List<CartItem> getAllCartItems() {
        return cartItemService.getAllCartItems();
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<CartItem> getCartItemById(@PathVariable String id) {
        Optional<CartItem> result = cartItemService.getCartItemsById(id);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

}

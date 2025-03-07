package iuh.cartservice.services.Impl;

import iuh.cartservice.entities.Cart;
import iuh.cartservice.entities.CartItem;
import iuh.cartservice.repositories.CartItemRepository;
import iuh.cartservice.repositories.CartRepository;
import iuh.cartservice.services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;

    @Override
    public Iterable<Cart> getAllCarts() {return cartRepository.findAll();}

    @Override
    public Optional<Cart> createCart(Cart cart) {
        return Optional.of(cartRepository.save(cart));
    }

    @Override
    public Optional<Cart> getCartById(String id) {
        return Optional.of(cartRepository.findById(id).get());
    }


}

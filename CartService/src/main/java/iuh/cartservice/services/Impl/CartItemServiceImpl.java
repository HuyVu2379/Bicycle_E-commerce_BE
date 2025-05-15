
package iuh.cartservice.services.Impl;

import iuh.cartservice.entities.CartItem;
import iuh.cartservice.repositories.CartItemRepository;
import iuh.cartservice.services.CartItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartItemServiceImpl implements CartItemService {
    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    public CartItemServiceImpl(CartItemRepository cartItemRepository) {
        this.cartItemRepository = cartItemRepository;
    }

    @Override
    public Optional<CartItem> addCartItem(CartItem cartItem) {
        return Optional.of(cartItemRepository.save(cartItem));
    }

    @Override
    public Optional<CartItem> updateCartItem(String id, int quantity) {
        Optional<CartItem> cartItem = cartItemRepository.findById(id);
        if (cartItem.isPresent()) {
            cartItem.get().setQuantity(quantity);
            return Optional.of(cartItemRepository.save(cartItem.get()));
        }
        return Optional.empty();
    }

    @Override
    public boolean removeCartItem(String id) {
        if (!cartItemRepository.existsById(id)) {
            return false;
        } else {
            cartItemRepository.deleteById(id);
            return true;
        }
    }

    @Override
    public boolean removeAllCartItems() {
        return false;
    }

    @Override
    public Optional<CartItem> getCartItemsById(String cartId) {
        return cartItemRepository.findById(cartId);
    }

    @Override
    public List<CartItem> getAllCartItems() {
        return cartItemRepository.findAll();
    }

    @Override
    public boolean bulkDelete(List<String> array) {
        List<CartItem> cartItemList = cartItemRepository.findAllById(array);
        if (cartItemList.size() == array.size()) {
            cartItemRepository.deleteAll(cartItemList);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteAllCartItem(String cartId) {
        if(cartItemRepository.deleteAllByCartId(cartId) > 0){
            return true;
        }
        return false;
    }

    @Override
    public List<CartItem> getAllCartItemByUserId(String userId) {
        return cartItemRepository.findCartItemsByUserId(userId);
    }

}

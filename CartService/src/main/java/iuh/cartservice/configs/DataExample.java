package iuh.cartservice.configs;

import com.netflix.discovery.converters.Auto;
import iuh.cartservice.entities.Cart;
import iuh.cartservice.entities.CartItem;
import iuh.cartservice.repositories.CartItemRepository;
import iuh.cartservice.repositories.CartRepository;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

//@Component
public class DataExample implements CommandLineRunner {
    private CartRepository cartRepository;
    private CartItemRepository cartItemRepository;
    public DataExample(CartRepository cartRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }
    private Faker faker = new Faker();

    @Override
    public void run(String... args) {
        for (int i = 0; i < 10; i++) {
            Cart cart = new Cart();
            cart.setUserId(String.valueOf(faker.number().digits(5)));
            CartItem cartItem = new CartItem();
            cartItem.setProductId(faker.idNumber().valid().toString());
            cartItem.setQuantity(faker.number().numberBetween(1, 10));

            cartItem.setCart(cart);
            List<CartItem> cartItems = new ArrayList<>();
            cartItems.add(cartItem);
            cart.setItems(cartItems);

            cartRepository.save(cart);
        }
    }

}

package iuh.cartservice.repositories;

import iuh.cartservice.entities.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<Cart,String> {
    public Cart findByUserId(String userId);
}

package iuh.cartservice.repositories;

import iuh.cartservice.entities.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem,String> {
    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem c WHERE c.cart.cartId = ?1")
    public int deleteAllByCartId(String cartId);
}

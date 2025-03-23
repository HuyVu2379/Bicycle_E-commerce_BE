package iuh.orderservice.repositories;

import iuh.orderservice.entities.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findOrdersByUserId(String userId);

    List<Order> getOrderByOrderDateBetween(LocalDateTime orderDateAfter, LocalDateTime orderDateBefore);

    @Query(value = "SELECT * FROM orders WHERE EXTRACT(YEAR FROM order_date) = :orderDateYear", nativeQuery = true)
    List<Order> getOrdersByOrderDate_Year(@Param("orderDateYear") int orderDateYear);

    @Query("SELECT o.userId, SUM(o.totalPrice) " +
            "FROM Order o GROUP BY o.userId")
    List<Object[]> getOrdersByUserGroup();
}

package iuh.orderservice.repositories;

import iuh.orderservice.entities.Order;
import iuh.orderservice.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    @Query("update Order o set o.status = :orderStatus where o.orderId = :orderId")
    @Modifying
    @Transactional
    int updateOrder(String orderId, OrderStatus orderStatus);
    Page<Order> findOrdersByUserIdAndStatus(String userId, Pageable pageable, OrderStatus orderStatus);

    List<Order> getOrderByOrderDateBetween(LocalDateTime orderDateAfter, LocalDateTime orderDateBefore);

    @Query(value = "SELECT * FROM orders WHERE EXTRACT(YEAR FROM order_date) = :orderDateYear", nativeQuery = true)
    List<Order> getOrdersByOrderDate_Year(@Param("orderDateYear") int orderDateYear);

//    @Query("SELECT o.userId, SUM(o.totalPrice) " +
//            "FROM Order o GROUP BY o.userId")
//    List<Object[]> getOrdersByUserGroup();

    @Query(value = """
            SELECT 
                o.user_id AS userId,
                SUM(o.total_price) AS totalRevenue 
            FROM orders o 
            GROUP BY o.user_id
            ORDER BY :#{#sort}""",
            countQuery = "SELECT COUNT(DISTINCT o.user_id) FROM orders o",
            nativeQuery = true)
    Page<Object[]> getOrdersByUserGroup(Pageable pageable, @Param("sort") String sort);
}

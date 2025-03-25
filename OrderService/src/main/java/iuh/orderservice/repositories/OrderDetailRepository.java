package iuh.orderservice.repositories;

import iuh.orderservice.entities.Order;
import iuh.orderservice.entities.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, String> {
    List<OrderDetail> getOrderDetailsByOrder(Order order);

    @Query("SELECT od.productId, SUM(od.subtotal) " +
            "FROM OrderDetail od GROUP BY od.productId")
    List<Object[]> getOrdersByProductGroup();
}

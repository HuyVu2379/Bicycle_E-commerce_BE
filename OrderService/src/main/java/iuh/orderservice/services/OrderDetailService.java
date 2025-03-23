package iuh.orderservice.services;

import iuh.orderservice.entities.OrderDetail;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface OrderDetailService {
    List<OrderDetail> createOrderDetails(List<OrderDetail> orderDetails);
    List<OrderDetail> getOrderDetailsByOrderId(String orderId);
    boolean deleteOrderDetails(String orderId);
}

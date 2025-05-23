package iuh.orderservice.services;

import iuh.orderservice.dtos.requests.CreateOrderRequest;
import iuh.orderservice.dtos.requests.OrderRequest;
import iuh.orderservice.entities.Order;
import iuh.orderservice.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public interface OrderService {
    Optional<Order> createOrder(CreateOrderRequest request, String userId, String token);
    int updateOrder(String orderId, OrderStatus OrderStatus);

    Optional<Order> getOrderById(String orderId);
    boolean deleteOrder(String orderId, String userId);

    //get all orders paging
    Page<Order> getAllOrders(int pageNo, int pageSize, String sortBy, String sortDirection);

    //get orders by userId paging
    Page<Order> getOrdersPageByUserId(int pageNo, int pageSize, String sortBy, String sortDirection, String userId);

    //Thong ke doanh thu theo thoi gian
    BigDecimal getRevenueByTime(String startTime, String endTime);

    //Thong ke doanh thu theo tung thang trong nam
    Map<String, BigDecimal> getRevenueByYear(int year);

    //Thong ke doanh thu theo nguoi dung
    Page<Map<String, Object>> getRevenueByUsers(int pageNo, int pageSize, String sortBy, String sortDirection);
}

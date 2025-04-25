package iuh.orderservice.services;

import iuh.orderservice.dtos.requests.CreateOrderRequest;
import iuh.orderservice.entities.Order;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public interface OrderService {
    Optional<Order> createOrder(CreateOrderRequest request, String userId, String token);
    Optional<Order> getOrderById(String orderId);
    boolean deleteOrder(String orderId, String userId);
    //Lich su mua hang cua nguoi dung
    List<Order> getOrdersByUserId(String userId);
    //Thong ke doanh thu theo thoi gian
    double getRevenueByTime(String startTime, String endTime);
    //Thong ke doanh thu theo tung thang trong nam
    Map<String, Double> getRevenueByYear(int year);
    //Thong ke doanh thu theo nguoi dung
    Page<Map<String, Object>> getRevenueByUsers(int pageNo, int pageSize, String sortBy, String sortDirection);
}

package iuh.orderservice.services;

import iuh.orderservice.dtos.requests.CreateOrderRequest;
import iuh.orderservice.entities.Order;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface OrderService {
    Optional<Order> createOrder(CreateOrderRequest request, String userId);
    Optional<Order> getOrderById(String orderId);
    boolean deleteOrder(String orderId);
    //Lich su mua hang cua nguoi dung
    List<Order> getOrdersByUserId(String userId);
    //Thong ke doanh thu theo thoi gian
    double getRevenueByTime(String startTime, String endTime);
    //Thong ke doanh thu theo tung thang trong nam
    List<Double> getRevenueByMonth(String year);
    //Thong ke doanh thu theo san pham
    List<Double> getRevenueByProduct(String productId);
    //Thong ke doanh thu theo nguoi dung
    List<Double> getRevenueByUser(String userId);
}

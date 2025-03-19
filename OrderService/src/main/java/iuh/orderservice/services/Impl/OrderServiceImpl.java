package iuh.orderservice.services.Impl;

import iuh.orderservice.clients.ProductServiceClient;
import iuh.orderservice.dtos.requests.CreateOrderRequest;
import iuh.orderservice.dtos.requests.ProductRequest;
import iuh.orderservice.dtos.responses.PriceRespone;
import iuh.orderservice.entities.Order;
import iuh.orderservice.entities.OrderDetail;
import iuh.orderservice.repositories.OrderDetailRepository;
import iuh.orderservice.repositories.OrderRepository;
import iuh.orderservice.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    private ProductServiceClient productServiceClient;

    @Override
    public Optional<Order> createOrder(CreateOrderRequest request, String userId) {
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderDate(LocalDateTime.now());
        order.setPromotionId(request.getPromotionId());
        List<OrderDetail> orderDetails = new ArrayList<>();
        double totalPrice = 0;

        for (ProductRequest product : request.getProducts()) {
            PriceRespone priceRespone = productServiceClient.getPrice(product.getProductId());
            double price = priceRespone != null ? priceRespone.getData() : 0.0;
            double subtotal = price * product.getQuantity();
            totalPrice += subtotal;

            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setProductId(product.getProductId());
            orderDetail.setQuantity(product.getQuantity());
            orderDetail.setSubtotal(subtotal);
            orderDetail.setOrder(order);
            orderDetails.add(orderDetail);
        }

        order.setTotalPrice(totalPrice);
        order.setOrderDetails(orderDetails);
        orderRepository.save(order);

        for (OrderDetail orderDetail : orderDetails) {
            orderDetail.setOrder(order);
            OrderDetail save = orderDetailRepository.save(orderDetail);
            if (save == null) {
                return Optional.empty();
            }
        }
        return Optional.of(order);
    }

    @Override
    public Optional<Order> getOrderById(String orderId) {
        return Optional.empty();
    }

    @Override
    public boolean deleteOrder(String orderId) {
        return false;
    }

    @Override
    public List<Order> getOrdersByUserId(String userId) {
        return List.of();
    }

    @Override
    public double getRevenueByTime(String startTime, String endTime) {
        return 0;
    }

    @Override
    public List<Double> getRevenueByMonth(String year) {
        return List.of();
    }

    @Override
    public List<Double> getRevenueByProduct(String productId) {
        return List.of();
    }

    @Override
    public List<Double> getRevenueByUser(String userId) {
        return List.of();
    }
}

package iuh.orderservice.services.Impl;

import iuh.orderservice.clients.ProductServiceClient;
import iuh.orderservice.dtos.requests.CreateOrderRequest;
import iuh.orderservice.dtos.requests.ProductRequest;
import iuh.orderservice.dtos.responses.MessageResponse;
import iuh.orderservice.dtos.responses.ProductPriceRespone;
import iuh.orderservice.entities.Order;
import iuh.orderservice.entities.OrderDetail;
import iuh.orderservice.enums.OrderStatus;
import iuh.orderservice.repositories.OrderDetailRepository;
import iuh.orderservice.repositories.OrderRepository;
import iuh.orderservice.repositories.PromotionRepository;
import iuh.orderservice.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    private ProductServiceClient productServiceClient;
    @Autowired
    private PromotionRepository promotionRepository;

    @Override
    public Optional<Order> createOrder(CreateOrderRequest request, String userId, String token) {
//        System.out.println(token); //token da chua bearer
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderDate(LocalDateTime.now());
        order.setPromotionId(request.getPromotionId());
        List<OrderDetail> orderDetails = new ArrayList<>();

        double totalPrice = 0;
        double reducePercent = 0.0;
        if (request.getPromotionId() != null && !request.getPromotionId().isEmpty()) {
            var promotionOpt = promotionRepository.findById(request.getPromotionId());
            if (promotionOpt.isPresent()) {
                reducePercent = promotionOpt.get().getReducePercent() / 100.0;
                order.setPromotionId(request.getPromotionId()); // chỉ set nếu hợp lệ
            }
        }

        for (ProductRequest product : request.getProducts()) {
            if (product.getProductId() == null || product.getProductId().isEmpty()) {
                throw new IllegalArgumentException("Product ID cannot be null or empty");
            }

            ProductPriceRespone priceRespone = productServiceClient.getPrice(product.getProductId());
            double price_c = priceRespone != null ? priceRespone.getData() : 0.0;
            if (price_c <= 0.0) {
                throw new RuntimeException("Invalid price for product: " + product.getProductId());
            }

            double price = priceRespone.getData();
            double subtotal = price * product.getQuantity();
            totalPrice += subtotal;

            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setProductId(product.getProductId());
            orderDetail.setColor(product.getColor());
            orderDetail.setQuantity(product.getQuantity());
            orderDetail.setSubtotal(subtotal);
            orderDetail.setOrder(order);
            orderDetails.add(orderDetail);
        }

        totalPrice = totalPrice - totalPrice * reducePercent;
        order.setTotalPrice(totalPrice);
        order.setOrderDetails(orderDetails);
        orderRepository.save(order);

        for (OrderDetail orderDetail : orderDetails) {
            orderDetail.setOrder(order);
            OrderDetail save = orderDetailRepository.save(orderDetail);

            ResponseEntity<MessageResponse<Object>> response = productServiceClient.reduceInventory(
                    orderDetail.getProductId(),
                    orderDetail.getColor(),
                    orderDetail.getQuantity()
            );
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Reduce inventory failed for product: " + orderDetail.getProductId());
            }

            if (save == null) {
                return Optional.empty();
            }
        }
        return Optional.of(order);
    }


    @Override
    public int updateOrder(String orderId, OrderStatus orderStatus) {
        return orderRepository.updateOrder(orderId,orderStatus);
    }


    @Override
    public Optional<Order> getOrderById(String orderId) {
        return Optional.ofNullable(orderRepository.findById(orderId).orElse(null));
    }

    @Override
    public boolean deleteOrder(String orderId, String userId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null && order.getUserId().equals(userId)) {
            orderRepository.deleteById(orderId);
            return true;
        }
        return false;
    }

    @Override
    public Page<Order> getAllOrders(int pageNo, int pageSize, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        return orderRepository.findAll(pageable);
    }

    @Override
    public Page<Order> getOrdersPageByUserId(int pageNo, int pageSize, String sortBy, String sortDirection, String userId) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Order> orders = orderRepository.findOrdersByUserId(userId, pageable);
        if (orders.isEmpty()) {
            return null;
        }
        return orders;
    }

    @Override
    public BigDecimal getRevenueByTime(String startTime, String endTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        LocalDate startDate = LocalDate.parse(startTime, formatter);
        LocalDate endDate = LocalDate.parse(endTime, formatter);

        LocalDateTime start = startDate.atStartOfDay(); // 00:00:00
        LocalDateTime end = endDate.atTime(23, 59, 59); // 23:59:59

        List<Order> orders = orderRepository.getOrderByOrderDateBetween(start, end);
        if (orders != null) {
            return orders.stream()
                    .map(Order::getTotalPrice)
                    .map(BigDecimal::valueOf)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public Map<String, BigDecimal> getRevenueByYear(int year) {
        List<String> months = List.of("January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December");

        List<Order> orders = orderRepository.getOrdersByOrderDate_Year(year);
        Map<String, BigDecimal> revenueByMonth = new LinkedHashMap<>();

        for (int i = 0; i < months.size(); i++) {
            int monthIndex = i + 1;
            BigDecimal revenue = orders.stream()
                    .filter(order -> order.getOrderDate().getMonthValue() == monthIndex)
                    .map(Order::getTotalPrice) // giả sử trả về double
                    .map(BigDecimal::valueOf)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            revenueByMonth.put(months.get(i), revenue);
        }

        return revenueByMonth;
    }


    @Override
    public Page<Map<String, Object>> getRevenueByUsers(int pageNo, int pageSize, String sortBy, String sortDirection) {
        // Tạo Sort và Pageable
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        // Gọi repository với phân trang
        Page<Object[]> resultPage = orderRepository.getOrdersByUserGroup(pageable, sortBy);

        // Chuyển đổi sang Map
        return resultPage.map(obj -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("userId", obj[0].toString());
            DecimalFormat df = new DecimalFormat("#,##0.00");
            map.put("totalRevenue", df.format(BigDecimal.valueOf((Double) obj[1])));
            return map;
        });
    }

}

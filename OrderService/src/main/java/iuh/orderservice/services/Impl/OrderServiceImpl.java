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
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
        return Optional.of(orderRepository.findById(orderId).orElse(null));
    }

    @Override
    public boolean deleteOrder(String orderId) {
        return false;
    }

    @Override
    public List<Order> getOrdersByUserId(String userId) {
        List<Order> orders = orderRepository.findOrdersByUserId(userId);
        if (orders.isEmpty()) {
            return null;
        }
        return orders;
    }

    @Override
    public double getRevenueByTime(String startTime, String endTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        LocalDate startDate = LocalDate.parse(startTime, formatter);
        LocalDate endDate = LocalDate.parse(endTime, formatter);

        LocalDateTime start = startDate.atStartOfDay(); // 00:00:00
        LocalDateTime end = endDate.atTime(23, 59, 59); // 23:59:59

        List<Order> orders = orderRepository.getOrderByOrderDateBetween(start, end);
        if (orders != null) {
            return orders.stream().mapToDouble(Order::getTotalPrice).sum();
        }
        return 0.0;
    }

    @Override
    public Map<String, Double> getRevenueByYear(int year) {
        List<String> months = List.of("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");
        List<Order> orders = orderRepository.getOrdersByOrderDate_Year(year);
        Map<String, Double> revenueByMonth = new LinkedHashMap<>();
        for (int i = 0; i < months.size(); i++) {
            int monthIndex = i + 1;
            double revenue = orders.stream()
                    .filter(order -> order.getOrderDate().getMonthValue() == monthIndex)
                    .mapToDouble(Order::getTotalPrice)
                    .sum();
            revenueByMonth.put(months.get(i), revenue);
        }
        return revenueByMonth;
    }

    @Override
    public List<Double> getRevenueByProduct(String productId) {


        return List.of();
    }

    @Override
    public Page<Map<String, Object>> getRevenueByUsers(int pageNo, int pageSize, String sortBy, String sortDirection) {
        // Sắp xếp dữ liệu
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        // Lấy danh sách doanh thu theo userId
        List<Object[]> results = orderRepository.getOrdersByUserGroup();

        // Chuyển đổi danh sách Object[] thành danh sách Map<String, Object>
        List<Map<String, Object>> revenueList = results.stream()
                .map(obj -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("userId", obj[0].toString());
                    map.put("totalRevenue", (Double) obj[1]);
                    return map;
                })
                .collect(Collectors.toList());

        // Phân trang thủ công
        int start = Math.min(pageNo * pageSize, revenueList.size());
        int end = Math.min((pageNo + 1) * pageSize, revenueList.size());
        List<Map<String, Object>> pagedRevenueList = revenueList.subList(start, end);

        return new PageImpl<>(pagedRevenueList, pageable, revenueList.size());
    }

}

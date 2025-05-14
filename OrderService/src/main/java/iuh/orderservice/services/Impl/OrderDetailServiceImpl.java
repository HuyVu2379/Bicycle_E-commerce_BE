package iuh.orderservice.services.Impl;

import iuh.orderservice.clients.ProductServiceClient;
import iuh.orderservice.dtos.responses.ProductNameRespone;
import iuh.orderservice.entities.Order;
import iuh.orderservice.entities.OrderDetail;
import iuh.orderservice.repositories.OrderDetailRepository;
import iuh.orderservice.repositories.OrderRepository;
import iuh.orderservice.services.OrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderDetailServiceImpl implements OrderDetailService {
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductServiceClient productServiceClient;

    @Override
    public List<OrderDetail> getOrderDetailsByOrderId(String orderId, String userId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if(order != null && order.getUserId().equals(userId)){
            return orderDetailRepository.getOrderDetailsByOrder(order);
        }
        return null;
    }

    @Override
    public Page<Map<String, Object>> getRevenueByProducts(int pageNo, int pageSize, String sortBy, String sortDirection) {
        // Sắp xếp dữ liệu
//        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
//        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        // Lấy danh sách doanh thu theo productId
        List<Object[]> results = orderDetailRepository.getOrdersByProductGroup();

        // Chuyển đổi danh sách Object[] thành danh sách Map<String, Object>
        List<Map<String, Object>> revenueList = results.stream()
                .map(obj -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    ProductNameRespone productNameRespone = productServiceClient.getName(obj[0].toString());
                    String productName = productNameRespone != null ? productNameRespone.getData() : "Unknown";
                    map.put("productName", productName);
                    DecimalFormat df = new DecimalFormat("#,##0.00");
                    map.put("totalRevenue", df.format(BigDecimal.valueOf((Double) obj[1])));
                    return map;
                })
                .collect(Collectors.toList());

        // **Sắp xếp thủ công trước khi phân trang**
        Comparator<Map<String, Object>> comparator = Comparator.comparing(m -> (Double) m.get("totalRevenue"));
        if ("desc".equalsIgnoreCase(sortDirection)) {
            comparator = comparator.reversed();
        }
        revenueList.sort(comparator);

        // **Phân trang thủ công**
        int start = Math.min(pageNo * pageSize, revenueList.size());
        int end = Math.min((pageNo + 1) * pageSize, revenueList.size());
        List<Map<String, Object>> pagedRevenueList = revenueList.subList(start, end);

        return new PageImpl<>(pagedRevenueList, PageRequest.of(pageNo, pageSize), revenueList.size());
    }

}

package iuh.orderservice.services;

import iuh.orderservice.entities.OrderDetail;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public interface OrderDetailService {
    List<OrderDetail> getOrderDetailsByOrderId(String orderId, String userId);
    //Thong ke doanh thu theo san pham
    Page<Map<String, Object>> getRevenueByProducts(int pageNo, int pageSize, String sortBy, String sortDirection);
}

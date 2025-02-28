package iuh.orderservice.mappers;

import iuh.orderservice.dtos.requests.OrderRequest;
import iuh.orderservice.entities.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    Order ORquestToOrder(OrderRequest orderRequest);
    OrderRequest orderToORquest(Order order);
}

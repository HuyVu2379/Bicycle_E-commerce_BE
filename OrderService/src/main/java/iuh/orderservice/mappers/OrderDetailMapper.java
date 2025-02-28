package iuh.orderservice.mappers;

import iuh.orderservice.dtos.requests.OrderDetailRequest;
import iuh.orderservice.entities.OrderDetail;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderDetailMapper {
    OrderDetail ODRequestToOrderDetail(OrderDetailRequest orderDetailRequest);
    OrderDetailRequest orderDetailToODRequest(OrderDetail orderDetail);
}

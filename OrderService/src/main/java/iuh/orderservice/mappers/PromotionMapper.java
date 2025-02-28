package iuh.orderservice.mappers;

import iuh.orderservice.dtos.requests.PromotionRequest;
import iuh.orderservice.entities.Promotion;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PromotionMapper {
    Promotion PromotionRequestToPromotion(PromotionRequest promotionRequest);
    PromotionRequest PromotionToPromotionRequest(Promotion promotion);
}

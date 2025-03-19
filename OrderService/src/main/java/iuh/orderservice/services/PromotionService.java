package iuh.orderservice.services;

import iuh.orderservice.entities.Promotion;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface PromotionService {
    Optional<Promotion> createPromotion(Promotion promotion);
    Optional<Promotion> updatePromotion(Promotion promotion);
    Optional<Promotion> getPromotionById(String promotionId);
    boolean deletePromotion(String promotionId);
}

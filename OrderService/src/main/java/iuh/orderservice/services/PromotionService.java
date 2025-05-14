package iuh.orderservice.services;

import iuh.orderservice.entities.Promotion;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface PromotionService {
    Optional<Promotion> createPromotion(Promotion promotion);
    Optional<Promotion> updatePromotion(Promotion promotion);
    void deletePromotionById(String id);

    Optional<Promotion> getPromotionById(String id);

    List<Promotion> getAllPromotions();

    Optional<Promotion> togglePromotionStatus(String id);
}

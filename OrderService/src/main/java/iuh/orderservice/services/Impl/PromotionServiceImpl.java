package iuh.orderservice.services.Impl;

import iuh.orderservice.entities.Promotion;
import iuh.orderservice.repositories.PromotionRepository;
import iuh.orderservice.services.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {
    private final PromotionRepository promotionRepository;


    @Override
    public Optional<Promotion> createPromotion(Promotion promotion) {
        return Optional.of(promotionRepository.save(promotion));
    }

    @Override
    public Optional<Promotion> updatePromotion(Promotion promotion) {
        return Optional.of(promotionRepository.saveAndFlush(promotion));
    }

    @Override
    public void deletePromotionById(String id) {
        promotionRepository.deleteById(id);
    }

    @Override
    public Optional<Promotion> getPromotionById(String id) {
        return promotionRepository.findById(id);
    }

    @Override
    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }

    @Override
    public Optional<Promotion> togglePromotionStatus(String id) {
        Optional<Promotion> promotionOptional = promotionRepository.findById(id);
        if (promotionOptional.isPresent()) {
            Promotion promotion = promotionOptional.get();
            promotion.setActive(!promotion.isActive());
            return Optional.of(promotionRepository.save(promotion));
        }
        return Optional.empty();
    }
}

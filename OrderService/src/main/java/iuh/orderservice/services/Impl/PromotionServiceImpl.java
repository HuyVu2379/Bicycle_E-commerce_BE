package iuh.orderservice.services.Impl;

import iuh.orderservice.entities.Promotion;
import iuh.orderservice.repositories.PromotionRepository;
import iuh.orderservice.services.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PromotionServiceImpl implements PromotionService {
    private PromotionRepository promotionRepository;

    @Autowired
    public PromotionServiceImpl(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    @Override
    public Promotion createPromotion(Promotion promotion) {
        return promotionRepository.save(promotion);
    }

    @Override
    public Promotion updatePromotion(Promotion promotion) {
        return promotionRepository.saveAndFlush(promotion);
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
        return promotionRepository.findAll()    ;
    }
}

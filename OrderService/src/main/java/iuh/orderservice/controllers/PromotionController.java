package iuh.orderservice.controllers;

import iuh.orderservice.entities.Promotion;
import iuh.orderservice.services.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/promotions")
public class PromotionController {
    private final PromotionService promotionService;

    @Autowired
    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @PostMapping("/createPromotion")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Promotion> createPromotion(@RequestBody Promotion promotion) {
        promotion = promotionService.createPromotion(promotion);
        return ResponseEntity.status(HttpStatus.CREATED).body(promotion);
    }

    @PutMapping("/updatePromotion/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Optional<Promotion>> updatePromotion(@PathVariable String id, @RequestBody Promotion promotion) {
        Optional<Promotion> oldPromotion = promotionService.getPromotionById(id);
        if(oldPromotion.isPresent()) {
            Promotion newPromotion = oldPromotion.get();
            newPromotion.setName(promotion.getName());
            newPromotion.setReducePercent(promotion.getReducePercent());
            newPromotion.setLimitValue(promotion.getLimitValue());
            newPromotion.setStartDate(promotion.getStartDate());
            newPromotion.setEndDate(promotion.getEndDate());
            newPromotion.setActive(promotion.isActive());
            newPromotion.setApplyFor(promotion.getApplyFor());
            promotionService.updatePromotion(newPromotion);
            return ResponseEntity.ok(oldPromotion);
        }else{
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/deletePromotion/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> deletePromotion(@PathVariable String id) {
        Optional<Promotion> promotion = promotionService.getPromotionById(id);
        if (promotion.isPresent()) {
            promotionService.deletePromotionById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public Optional<Promotion> getPromotionById(@PathVariable String id) {
        return promotionService.getPromotionById(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public List<Promotion> getAllPromotions() {
        return promotionService.getAllPromotions();
    }
}

package iuh.orderservice.controllers;

import iuh.orderservice.dtos.responses.MessageResponse;
import iuh.orderservice.dtos.responses.SuccessEntityResponse;
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
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<MessageResponse<Object>> createPromotion(@RequestBody Promotion promotion) {
        Optional<Promotion> promotionOptional = promotionService.createPromotion(promotion);
        if (promotionOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new MessageResponse<>(HttpStatus.BAD_REQUEST.value(), "Promotion creation failed", false, null)
            );
        }
        return SuccessEntityResponse.created("Promotion created successfully", promotion);
    }

    @PutMapping("/updatePromotion/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<MessageResponse<Object>> updatePromotion(@PathVariable String id, @RequestBody Promotion promotion) {
        Optional<Promotion> oldPromotion = promotionService.getPromotionById(id);
        if (oldPromotion.isPresent()) {
            Promotion newPromotion = oldPromotion.get();
            newPromotion.setName(promotion.getName());
            newPromotion.setReducePercent(promotion.getReducePercent());
            newPromotion.setLimitValue(promotion.getLimitValue());
            newPromotion.setStartDate(promotion.getStartDate());
            newPromotion.setEndDate(promotion.getEndDate());
            newPromotion.setActive(promotion.isActive());
            newPromotion.setApplyFor(promotion.getApplyFor());
            promotionService.updatePromotion(newPromotion);
            return SuccessEntityResponse.created("Promotion updated successfully", newPromotion);
        } else {
            return ResponseEntity.badRequest().body(
                    new MessageResponse<>(HttpStatus.BAD_REQUEST.value(), "Promotion not found", false, null)
            );
        }
    }

    @DeleteMapping("/deletePromotion/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<MessageResponse<Void>> deletePromotion(@PathVariable String id) {
        Optional<Promotion> promotion = promotionService.getPromotionById(id);
        if (promotion.isPresent()) {
            promotionService.deletePromotionById(id);
            return SuccessEntityResponse.ok("Promotion deleted successfully", null);
        } else {
            return ResponseEntity.badRequest().body(
                    new MessageResponse<>(HttpStatus.BAD_REQUEST.value(), "Promotion not found", false, null)
            );
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<MessageResponse<Object>> getPromotionById(@PathVariable String id) {
        Optional<Promotion> promotion = promotionService.getPromotionById(id);
        if(promotion.isPresent()) {
            return SuccessEntityResponse.ok("Promotion found", promotion.get());
        } else {
            return ResponseEntity.badRequest().body(
                    new MessageResponse<>(HttpStatus.BAD_REQUEST.value(), "Promotion not found", false, null)
            );
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<MessageResponse<Object>> getAllPromotions() {
        List<Promotion> promotions = promotionService.getAllPromotions();
        if(promotions.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new MessageResponse<>(HttpStatus.BAD_REQUEST.value(), "No promotion found", false, null)
            );
        }
        return SuccessEntityResponse.found("Promotions found", promotions);
    }
}

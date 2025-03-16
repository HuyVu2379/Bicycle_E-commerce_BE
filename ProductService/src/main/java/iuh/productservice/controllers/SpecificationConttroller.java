package iuh.productservice.controllers;

import iuh.productservice.dtos.responses.MessageResponse;
import iuh.productservice.dtos.responses.SuccessEntityResponse;
import iuh.productservice.entities.Specification;
import iuh.productservice.services.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/specifications")
public class SpecificationConttroller {
    @Autowired
    private SpecificationService specificationService;

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<MessageResponse<List<Specification>>> createSpecifications(@RequestBody List<Specification> specifications) {
        List<Specification> createdSpecifications = specificationService.createSpecifications(specifications);
        if (createdSpecifications.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new MessageResponse<>(400, "Specification creation failed", false, null)
            );
        }
        return ResponseEntity.ok(
                new MessageResponse<>(201, "Specifications created successfully", true, createdSpecifications)
        );
    }

    @PostMapping("/update")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<MessageResponse<List<Specification>>> updateSpecifications(@RequestBody List<Specification> specifications) {
        List<Specification> updatedSpecifications = specificationService.updateSpecifications(specifications);
        if (updatedSpecifications.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new MessageResponse<>(400, "Specification update failed", false, null)
            );
        }
        return ResponseEntity.ok(
                new MessageResponse<>(201, "Specifications update successfully", true, updatedSpecifications)
        );
    }

    @PostMapping("/delete-all/{productid}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<MessageResponse<Boolean>> deleteAllSpecifications(@PathVariable String productid) {
        boolean specificationResponse = specificationService.deleteAllSpecificationByProductId(productid);
        if (!specificationResponse) {
            return ResponseEntity.badRequest().body(
                    new MessageResponse<>(400,
                            "Specifications deletion failed",
                            false,
                            null
                    ));
        }
        return SuccessEntityResponse.ok("Specifications deleted successfully", specificationResponse);
    }

    @GetMapping("/find/{productid}")
    public ResponseEntity<MessageResponse<List<Specification>>> findSpecifications(@PathVariable String productid) {
        List<Specification> specificationsResponse = specificationService.findSpecificationsByProductId(productid);
        if (specificationsResponse.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new MessageResponse<>(400, "Specifications scan failed", false, null)
            );
        }
        return ResponseEntity.ok(
                new MessageResponse<>(201, "Specifications found", true, specificationsResponse)
        );
    }
}

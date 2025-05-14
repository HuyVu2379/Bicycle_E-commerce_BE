/*
 * @ (#) InventoryController.java       1.0     3/13/2025
 *
 * Copyright (c) 2025. All rights reserved.
 */

package iuh.productservice.controllers;


import iuh.productservice.dtos.responses.MessageResponse;
import iuh.productservice.dtos.responses.SuccessEntityResponse;
import iuh.productservice.entities.Inventory;
import iuh.productservice.services.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/inventories")
public class InventoryController {
    private final InventoryService inventoryService;

    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/createInventory")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse<Object>> createInventory(@RequestBody Inventory inventory) {
        Optional<Inventory> inventoryOptional = inventoryService.createInventory(inventory);
        if (inventoryOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new MessageResponse<>(HttpStatus.BAD_REQUEST.value(), "Inventory created failed", false, null));
        }
        return SuccessEntityResponse.created("Inventory created successfully", inventory);
    }
    @PostMapping("/bulkCreateInventory")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse<List<Inventory>>> createInventory(@RequestBody List<Inventory> inventory) {
        List<Inventory> inventories = inventoryService.bulkCreateInventory(inventory);
        if (inventories.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new MessageResponse<>(HttpStatus.BAD_REQUEST.value(), "Inventory created failed", false, null));
        }
        return SuccessEntityResponse.created("Inventories created successfully", inventory);
    }

    @PutMapping("/updateInventory")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse<Object>> updateInventory(@RequestBody Inventory inventory) {
        Optional<Inventory> inventoryOptional = inventoryService.updateInventory(inventory);
        if (inventoryOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new MessageResponse<>(HttpStatus.BAD_REQUEST.value(), "Inventory updated failed", false, null)
            );
        }
        return SuccessEntityResponse.ok("Inventory updated successfully", inventory);
    }

    @DeleteMapping("/deleteInventory/{inventoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse<Void>> deleteInventory(@PathVariable String inventoryId) {
        Optional<Inventory> inventory = inventoryService.getInventoryById(inventoryId);
        if (inventory.isPresent()){
            inventoryService.deleteInventory(inventoryId);
            return SuccessEntityResponse.ok("Inventory deleted successfully", null);
        }
        return ResponseEntity.badRequest().body(
                new MessageResponse<>(HttpStatus.BAD_REQUEST.value(), "Inventory deleted failed", false, null)
        );
    }

    @GetMapping("/public/getAllInventories")
    public ResponseEntity<MessageResponse<Object>> getAllInventories() {
        List<Inventory> inventoryList = inventoryService.getAllInventories();
        if (inventoryList.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new MessageResponse<>(HttpStatus.BAD_REQUEST.value(), "No inventory found", false, null)
            );
        }
        return SuccessEntityResponse.found("Inventory found", inventoryList);

    }

    @GetMapping("/public/getAllInventoryByProductId/{productId}")
    public ResponseEntity<MessageResponse<List<Inventory>>> getAllInventoryByProductId(@PathVariable String productId) {
        List<Inventory> inventory = inventoryService.getAllInventoryByProductId(productId);
        if (inventory.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new MessageResponse<>(HttpStatus.BAD_REQUEST.value(), "No inventory found", false, null)
            );
        }
        return SuccessEntityResponse.found("Inventory found", inventory);
    }

    @PostMapping("/public/reduce-quantity/{productId}/{color}/{quantity}")
    public ResponseEntity<MessageResponse<Object>> reduceInventory(
            @PathVariable String productId,
            @PathVariable String color,
            @PathVariable int quantity) {
        boolean success = inventoryService.reduceInventory(productId, color, quantity);
        if (success) {
            return ResponseEntity.ok(new MessageResponse<>(HttpStatus.OK.value(), "Inventory had been reduced", true));
        } else {
            return ResponseEntity.badRequest().body(
                    new MessageResponse<>(HttpStatus.BAD_REQUEST.value(), "Inventory quantity update failed", false, null)
            );
        }
    }
}

/*
 * @ (#) InventoryController.java       1.0     3/13/2025
 *
 * Copyright (c) 2025. All rights reserved.
 */

package iuh.productservice.controllers;


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
    public ResponseEntity<Inventory> createInventory(@RequestBody Inventory inventory) {
        Optional<Inventory> inventoryOptional = inventoryService.createInventory(inventory);
        if (inventoryOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(inventory);
    }

    @PutMapping("/updateInventory")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Inventory> updateInventory(@RequestBody Inventory inventory) {
        Optional<Inventory> inventoryOptional = inventoryService.updateInventory(inventory);
        if (inventoryOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.OK).body(inventory);
    }

    @DeleteMapping("/deleteInventory/{inventoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteInventory(@PathVariable String inventoryId) {
        inventoryService.deleteInventory(inventoryId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/public/getAllInventories")
    public ResponseEntity<List<Inventory>> getAllInventories() {
        List<Inventory> inventoryList = inventoryService.getAllInventories();
        if(inventoryList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(HttpStatus.OK).body(inventoryList);

    }

    @GetMapping("/public/getAllInventoryByProductId/{productId}")
    public ResponseEntity<Optional<Inventory>> getAllInventoryByProductId(@PathVariable String productId) {
        Optional<Inventory> inventory = inventoryService.getAllInventoryByProductId(productId);
        if(inventory.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(HttpStatus.OK).body(inventory);
    }
}

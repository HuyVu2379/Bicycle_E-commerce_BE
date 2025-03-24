package iuh.productservice.services;

import iuh.productservice.entities.Inventory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface InventoryService {
    Optional<Inventory> createInventory(Inventory inventory);
    Optional<Inventory> updateInventory(Inventory inventory);
    void deleteInventory(String inventoryId);
    Optional<Inventory> getInventoryById(String inventoryId);
    Optional<Inventory> getAllInventoryByProductId(String productId);
    List<Inventory> getAllInventories();
}

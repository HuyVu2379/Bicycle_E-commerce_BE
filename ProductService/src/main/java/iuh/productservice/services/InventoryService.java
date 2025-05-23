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
    List<Inventory> getAllInventoryByProductId(String productId);
    List<Inventory> getAllInventories();
    boolean reduceInventory(String productId,String color, int quantityToDeduct);
    List<Inventory> bulkCreateInventory(List<Inventory> inventories);
}

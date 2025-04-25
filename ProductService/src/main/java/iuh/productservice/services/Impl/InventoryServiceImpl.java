package iuh.productservice.services.Impl;

import iuh.productservice.entities.Inventory;
import iuh.productservice.repositories.InventoryRepository;
import iuh.productservice.services.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryServiceImpl implements InventoryService {
    private InventoryRepository inventoryRepository;

    @Autowired
    public InventoryServiceImpl(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public Optional<Inventory> createInventory(Inventory inventory) {
        return Optional.of(inventoryRepository.save(inventory));
    }

    @Override
    public Optional<Inventory> updateInventory(Inventory inventory) {
        Optional<Inventory> inventory1 = inventoryRepository.findById(inventory.getInventoryId());
        if(inventory1.isPresent()) {
            inventory1.get().setQuantity(inventory.getQuantity());
            return Optional.of(inventoryRepository.save(inventory1.get()));
        }
        return inventory1;
    }

    @Override
    public void deleteInventory(String inventoryId) {
        Optional<Inventory> inventory = inventoryRepository.findById(inventoryId);
        inventory.ifPresent(value -> inventoryRepository.delete(value));
    }

    @Override
    public Optional<Inventory> getInventoryById(String inventoryId) {
        return inventoryRepository.findById(inventoryId);
    }

    @Override
    public Optional<Inventory> getAllInventoryByProductId(String productId) {
        return inventoryRepository.findAllByProductId(productId);
    }

    @Override
    public List<Inventory> getAllInventories() {
        return inventoryRepository.findAll();
    }

    @Override
    @Transactional
    public boolean reduceInventory(String productId, int quantityToDeduct) {
        List<Inventory> inventories = inventoryRepository.findByProductIdOrderByImportDateAsc(productId);
        int remaining = quantityToDeduct;

        for (Inventory inventory : inventories) {
            if (remaining <= 0) break;
            int currentQuantity = inventory.getQuantity();
            if (currentQuantity >= remaining) {
                inventory.setQuantity(currentQuantity - remaining);
                inventoryRepository.save(inventory);
                remaining = 0;
            } else {
                inventory.setQuantity(0);
                inventoryRepository.save(inventory);
                remaining -= currentQuantity;
            }
        }
        return remaining == 0; // true nếu trừ thành công hết số lượng yêu cầu
    }
}

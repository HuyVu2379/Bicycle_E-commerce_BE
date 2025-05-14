package iuh.productservice.repositories;

import iuh.productservice.entities.Inventory;
import iuh.productservice.enums.Color;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends MongoRepository<Inventory, String> {
    List<Inventory> findAllByProductId(String productId);
    List<Inventory> findByProductIdAndColorOrderByImportDateAsc(String productId, Color color);
}

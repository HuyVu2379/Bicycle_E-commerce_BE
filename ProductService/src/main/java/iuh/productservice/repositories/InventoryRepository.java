package iuh.productservice.repositories;

import iuh.productservice.entities.Inventory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends MongoRepository<Inventory, String> {
    Optional<Inventory> findAllByProductId(String productId);
    List<Inventory> findByProductIdOrderByImportDateAsc(String productId);
}

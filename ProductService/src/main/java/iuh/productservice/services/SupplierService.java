package iuh.productservice.services;

import iuh.productservice.entities.Supplier;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface SupplierService {
    Optional<Supplier> createSupplier(Supplier supplier);
    Optional<Supplier> updateSupplier(Supplier supplier);
    Optional<Supplier> getSupplierById(String supplierId);
    Optional<Supplier> getSupplierByEmail(String email);
    boolean deleteSupplier(String supplierId);
}

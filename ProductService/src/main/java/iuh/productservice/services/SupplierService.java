package iuh.productservice.services;

import iuh.productservice.dtos.requests.SupplierRequest;
import iuh.productservice.entities.Supplier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface SupplierService {
    Optional<Supplier> createSupplier(SupplierRequest supplierDTO);
    List<Supplier> getAllSuppliers();
    Optional<Supplier> updateSupplier(Supplier supplier);
    Optional<Supplier> getSupplierById(String supplierId);
    Optional<Supplier> getSupplierByEmail(String email);
    boolean deleteSupplier(String supplierId);
}

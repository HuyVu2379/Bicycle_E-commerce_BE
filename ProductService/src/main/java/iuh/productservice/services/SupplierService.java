package iuh.productservice.services;

import iuh.productservice.dtos.requests.SupplierRequest;
import iuh.productservice.dtos.responses.SupplierResponse;
import iuh.productservice.entities.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface SupplierService {
    Optional<Supplier> createSupplier(SupplierRequest supplierDTO);
    List<Supplier> getAllSuppliers();
    //get all suppliers with pagination
    Page<SupplierResponse> getAllSuppliers(int pageNo, int pageSize, String sortBy, String sortDirection);
    Optional<Supplier> updateSupplier(SupplierRequest supplierRequest);
    Optional<Supplier> getSupplierById(String supplierId);
    Optional<Supplier> getSupplierByEmail(String email);
    boolean deleteSupplier(String supplierId);
}

package iuh.productservice.services.Impl;

import iuh.productservice.entities.Supplier;
import iuh.productservice.repositories.SupplierRepository;
import iuh.productservice.services.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SupplierServiceImpl implements SupplierService {
    @Autowired
    private SupplierRepository supplierRepository;

    @Override
    public Optional<Supplier> createSupplier(Supplier supplier) {
        return Optional.of(supplierRepository.save(supplier));
    }

    @Override
    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    @Override
    public Optional<Supplier> updateSupplier(Supplier supplier) {
        if(supplierRepository.findById(supplier.getSupplierId()).isEmpty()){
            return Optional.empty();
        }
        return Optional.of(supplierRepository.save(supplier));
    }

    @Override
    public Optional<Supplier> getSupplierById(String supplierId) {
        return supplierRepository.findById(supplierId);
    }

    @Override
    public Optional<Supplier> getSupplierByEmail(String email) {
        return supplierRepository.getSupplierByEmail(email);
    }

    @Override
    public boolean deleteSupplier(String supplierId) {
        if(supplierRepository.findById(supplierId).isEmpty()){
            return false;
        }
        supplierRepository.deleteById(supplierId);
        return true;
    }
}

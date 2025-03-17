package iuh.productservice.services.Impl;
import iuh.productservice.client.UserServiceClient;
import iuh.productservice.dtos.requests.AddressRequest;
import iuh.productservice.dtos.requests.SupplierRequest;
import iuh.productservice.dtos.responses.AddressResponse;
import iuh.productservice.dtos.responses.MessageResponse;
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

    @Autowired
    private UserServiceClient userServiceClient;

    @Override
    public Optional<Supplier> createSupplier(SupplierRequest supplierRequest) {
        Supplier supplier = new Supplier();
        supplier.setName(supplierRequest.getName());
        supplier.setPhone(supplierRequest.getPhone());
        supplier.setEmail(supplierRequest.getEmail());
        supplier.setDescription(supplierRequest.getDescription());
        supplier.setAddressId(null);
        supplierRepository.save(supplier); //luu supplier truoc de lay supplierId

        //Gui request tao address
        AddressRequest addressRequest = new AddressRequest();
        addressRequest.setCity(supplierRequest.getAddress().getCity());
        addressRequest.setDistrict(supplierRequest.getAddress().getDistrict());
        addressRequest.setStreet(supplierRequest.getAddress().getStreet());
        addressRequest.setWard(supplierRequest.getAddress().getWard());
        addressRequest.setCountry(supplierRequest.getAddress().getCountry());
        addressRequest.setUserId(supplier.getSupplierId());
        MessageResponse<AddressResponse> response = userServiceClient.createAddress(addressRequest);
        AddressResponse addressResponse = response.getData();
        supplier.setAddressId(addressResponse.getAddressId());
        supplierRepository.save(supplier);

        return Optional.of(supplier);
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

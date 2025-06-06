package iuh.productservice.services.Impl;
import iuh.productservice.client.UserServiceClient;
import iuh.productservice.dtos.requests.AddressRequest;
import iuh.productservice.dtos.requests.SupplierRequest;
import iuh.productservice.dtos.responses.AddressResponse;
import iuh.productservice.dtos.responses.MessageResponse;
import iuh.productservice.dtos.responses.SupplierResponse;
import iuh.productservice.entities.Supplier;
import iuh.productservice.repositories.SupplierRepository;
import iuh.productservice.services.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

        if(supplierRequest.getAddress().getCountry() == null || supplierRequest.getAddress().getCountry().isEmpty()){
            addressRequest.setCountry("Viet Nam");
        }else{
            addressRequest.setCountry(supplierRequest.getAddress().getCountry());
        }

        addressRequest.setUserId(supplier.getSupplierId());

        if(supplierRequest.getAddress().getFullAddress() == null || supplierRequest.getAddress().getFullAddress().isEmpty()){
            String fullAddress = supplierRequest.getAddress().getStreet() + ", " +
                    supplierRequest.getAddress().getWard() + ", " +
                    supplierRequest.getAddress().getDistrict() + ", " +
                    supplierRequest.getAddress().getCity()+ ", " +
                    supplierRequest.getAddress().getCountry();
            addressRequest.setFullAddress(fullAddress);
        }else{
            addressRequest.setFullAddress(supplierRequest.getAddress().getFullAddress());
        }

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
    public Page<SupplierResponse> getAllSuppliers(int pageNo, int pageSize, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Supplier> suppliers = supplierRepository.findAll(pageable);
        return suppliers.map(supplier -> {
            AddressResponse addressResponse = userServiceClient.getAddressByUserId(supplier.getSupplierId()).getData();
            return SupplierResponse.builder()
                    .supplierId(supplier.getSupplierId())
                    .name(supplier.getName())
                    .phone(supplier.getPhone())
                    .email(supplier.getEmail())
                    .description(supplier.getDescription())
                    .address(addressResponse)
                    .build();
        });
    }

    @Override
    public Optional<Supplier> updateSupplier(SupplierRequest supplierRequest) {
        Supplier foundSupplier = supplierRepository.findById(supplierRequest.getSupplierId()).orElse(null);
        if(supplierRepository.findById(supplierRequest.getSupplierId()).isEmpty()){
            return Optional.empty();
        }
        Supplier supplier = new Supplier();
        supplier.setSupplierId(supplierRequest.getSupplierId());
        supplier.setName(supplierRequest.getName());
        supplier.setPhone(supplierRequest.getPhone());
        supplier.setEmail(supplierRequest.getEmail());
        supplier.setDescription(supplierRequest.getDescription());
        supplier.setAddressId(foundSupplier.getAddressId());
        supplier.setCreatedAt(foundSupplier.getCreatedAt());

        //Gui request update address
        AddressRequest addressRequest = new AddressRequest();
        addressRequest.setAddressId(foundSupplier.getAddressId());
        addressRequest.setCity(supplierRequest.getAddress().getCity());
        addressRequest.setDistrict(supplierRequest.getAddress().getDistrict());
        addressRequest.setStreet(supplierRequest.getAddress().getStreet());
        addressRequest.setWard(supplierRequest.getAddress().getWard());
        addressRequest.setCountry(supplierRequest.getAddress().getCountry());
        String fullAddress = supplierRequest.getAddress().getStreet() + ", " +
                supplierRequest.getAddress().getWard() + ", " +
                supplierRequest.getAddress().getDistrict() + ", " +
                supplierRequest.getAddress().getCity()+ ", " +
                supplierRequest.getAddress().getCountry();
        addressRequest.setFullAddress(fullAddress);
        addressRequest.setUserId(supplier.getSupplierId());
        userServiceClient.updateAddress(addressRequest);

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

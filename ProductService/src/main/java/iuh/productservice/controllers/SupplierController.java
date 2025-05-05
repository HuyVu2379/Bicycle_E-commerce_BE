package iuh.productservice.controllers;

import iuh.productservice.client.UserServiceClient;
import iuh.productservice.dtos.requests.SupplierRequest;
import iuh.productservice.dtos.responses.AddressResponse;
import iuh.productservice.dtos.responses.MessageResponse;
import iuh.productservice.dtos.responses.SuccessEntityResponse;
import iuh.productservice.dtos.responses.SupplierResponse;
import iuh.productservice.entities.Supplier;
import iuh.productservice.services.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/suppliers")
public class SupplierController {
    @Autowired
    private SupplierService supplierService;
    @Autowired
    private UserServiceClient userServiceClient;

    @GetMapping("/public/getAll")
    public ResponseEntity<MessageResponse<Page<SupplierResponse>>> getAllSuppliers(@RequestParam(defaultValue = "0") int pageNo,
                                                                           @RequestParam(defaultValue = "10") int pageSize,
                                                                           @RequestParam(defaultValue = "createdAt") String sortBy,
                                                                           @RequestParam(defaultValue = "desc") String sortDirection) {
        Page<SupplierResponse> suppliers = supplierService.getAllSuppliers(pageNo, pageSize, sortBy, sortDirection);
        if (suppliers.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new MessageResponse<>(400,
                            "No suppliers found",
                            false,
                            null
                    ));
        }
        return SuccessEntityResponse.ok("Suppliers retrieved successfully", suppliers);
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<MessageResponse<Supplier>> createSupplier(@RequestBody SupplierRequest supplierRequest) {
        Optional<Supplier> supplierResponse = supplierService.createSupplier(supplierRequest);
        if (supplierResponse.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new MessageResponse<>(400,
                            "Supplier creation failed",
                            false,
                            null
                    ));
        }
        return SuccessEntityResponse.created("Supplier created successfully", supplierResponse.get());
    }

    @PostMapping("/update")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<MessageResponse<Supplier>> updateSupplier(@RequestBody SupplierRequest supplierRequest) {
        Optional<Supplier> supplierResponse = supplierService.updateSupplier(supplierRequest);
        if (supplierResponse.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new MessageResponse<>(400,
                            "Supplier update failed",
                            false,
                            null
                    ));
        }
        return SuccessEntityResponse.ok("Supplier updated successfully", supplierResponse.get());
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<MessageResponse<Boolean>> deleteSupplier(@PathVariable String id) {
        boolean supplierResponse = supplierService.deleteSupplier(id);
        if (!supplierResponse) {
            return ResponseEntity.badRequest().body(
                    new MessageResponse<>(400,
                            "Supplier deletion failed",
                            false,
                            null
                    ));
        }
        userServiceClient.deleteAddress(id);
        return SuccessEntityResponse.ok("Supplier deleted successfully", supplierResponse);
    }

    @GetMapping("/public/getSupplierById/{id}")
    public ResponseEntity<MessageResponse<SupplierResponse>> getSupplierById(@PathVariable String id) {
        AddressResponse addressResponse = userServiceClient.getAddressByUserId(id).getData();
        Supplier supplier = supplierService.getSupplierById(id).get();
        SupplierResponse supplierResponse = SupplierResponse.builder()
                .supplierId(supplier.getSupplierId())
                .name(supplier.getName())
                .phone(supplier.getPhone())
                .email(supplier.getEmail())
                .description(supplier.getDescription())
                .address(addressResponse)
                .build();
        if (supplierResponse == null) {
            return ResponseEntity.badRequest().body(
                    new MessageResponse<>(400,
                            "Supplier retrieval failed",
                            false,
                            null
                    ));
        }
        return SuccessEntityResponse.ok("Supplier retrieved successfully", supplierResponse);
    }

    @GetMapping("/public/getSupplierByEmail")
    public ResponseEntity<MessageResponse<SupplierResponse>> getSupplierByEmail(@RequestParam String email) {
        Optional<Supplier> supplierResponse = supplierService.getSupplierByEmail(email);
        AddressResponse addressResponse = userServiceClient.getAddressByUserId(supplierResponse.get().getSupplierId()).getData();
        SupplierResponse actualSupplierResponse = SupplierResponse.builder()
                .supplierId(supplierResponse.get().getSupplierId())
                .name(supplierResponse.get().getName())
                .phone(supplierResponse.get().getPhone())
                .email(supplierResponse.get().getEmail())
                .description(supplierResponse.get().getDescription())
                .address(addressResponse)
                .build();
        if (actualSupplierResponse == null) {
            return ResponseEntity.badRequest().body(
                    new MessageResponse<>(400,
                            "Supplier retrieval failed",
                            false,
                            null
                    ));
        }
        return SuccessEntityResponse.ok("Supplier retrieved successfully", actualSupplierResponse);
    }
}

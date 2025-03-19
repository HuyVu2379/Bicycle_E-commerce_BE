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
    public ResponseEntity<MessageResponse<List<Supplier>>> getAllSuppliers() {
        return SuccessEntityResponse.ok("Get all suppliers sucessfull", supplierService.getAllSuppliers());
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
    public ResponseEntity<MessageResponse<Supplier>> updateSupplier(@RequestBody Supplier supplier) {
        Optional<Supplier> supplierResponse = supplierService.updateSupplier(supplier);
        if (supplierResponse.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new MessageResponse<>(400,
                            "Supplier update failed",
                            false,
                            null
                    ));
        }
        return SuccessEntityResponse.ok("Supplier updated successfully", supplierService.updateSupplier(supplier).get());
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
        return SuccessEntityResponse.ok("Supplier deleted successfully", supplierResponse);
    }

    @GetMapping("/public/getSupplierById/{id}")
    public ResponseEntity<MessageResponse<SupplierResponse>> getSupplierById(@PathVariable String id) {
        AddressResponse addressResponse = userServiceClient.getAddressByUserId(id).getData();
        System.out.println("check address response: " + addressResponse);
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
    public ResponseEntity<MessageResponse<Supplier>> getSupplierByEmail(@RequestParam String email) {
        Optional<Supplier> supplierResponse = supplierService.getSupplierByEmail(email);
        if (supplierResponse.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new MessageResponse<>(400,
                            "Supplier retrieval failed",
                            false,
                            null
                    ));
        }
        return SuccessEntityResponse.ok("Supplier retrieved successfully", supplierResponse.get());
    }


}

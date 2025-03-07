package iuh.userservice.controllers;

import iuh.userservice.dtos.responses.AddressResponse;
import iuh.userservice.dtos.responses.MessageResponse;
import iuh.userservice.dtos.responses.SuccessEntityResponse;
import iuh.userservice.entities.Address;
import iuh.userservice.mappers.AddressMapper;
import iuh.userservice.services.Impl.AddressServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/users/address")
public class AddressController {
    @Autowired
    private AddressServiceImpl addressService;
    @Autowired
    private AddressMapper addressMapper;
    @PostMapping("/create")
    public ResponseEntity<MessageResponse<AddressResponse>> createAddress(@RequestBody Address address) {
        Address address1 = addressService.createAddress(address).get();
        AddressResponse addressResponse = addressMapper.AddressToAddressResponse(address1);
        return SuccessEntityResponse.created("Create address successfully", addressResponse);
    }
    @PostMapping("/update")
    public ResponseEntity<MessageResponse<Address>> updateAddress(@RequestBody Address address) {
        Address address1 = addressService.updateAddress(address).get();
        return SuccessEntityResponse.ok("Update address successfully", address1);
    }
    @DeleteMapping("/delete")
    public ResponseEntity<MessageResponse<Boolean>> deleteAddress(@RequestBody String userId) {
        addressService.deleteAddress(userId);
        return SuccessEntityResponse.ok("Delete address successfully", true);
    }
    @GetMapping("/{userId}")
    public ResponseEntity<MessageResponse<Address>> getAddressByUserId(@PathVariable String userId) {
        Address address = addressService.getAddressByUserId(userId).get();
        return SuccessEntityResponse.ok("Get address successfully", address);
    }
}

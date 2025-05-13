package iuh.userservice.services;

import iuh.userservice.entities.Address;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public interface AddressService {
    Optional<Address> getAddressByUserId(String userId);
    Optional<Address> createAddress(Address address);
    Optional<Address> updateAddress(Address address);
    @Transactional
    boolean deleteAddress(String userId);
}

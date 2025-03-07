package iuh.userservice.services;

import iuh.userservice.entities.Address;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public interface AddressService {
    public Optional<Address> getAddressByUserId(String userId);
    public Optional<Address> createAddress(Address address);
    public Optional<Address> updateAddress(Address address);
    @Transactional
    public boolean deleteAddress(String userId);
}

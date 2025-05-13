package iuh.userservice.services.Impl;

import iuh.userservice.entities.Address;
import iuh.userservice.repositories.AddressRepository;
import iuh.userservice.services.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AddressServiceImpl implements AddressService {
    @Autowired
    private AddressRepository addressRepository;

    @Override
    public Optional<Address> getAddressByUserId(String userId) {
        return addressRepository.findAddressByUserId(userId);
    }

    @Override
    public Optional<Address> createAddress(Address address) {
        return Optional.of(addressRepository.save(address));
    }

    @Override
    public Optional<Address> updateAddress(Address address) {
        return addressRepository.updateAddress(address);
    }

    @Override
    public boolean deleteAddress(String userId) {
        return addressRepository.deleteAddressByUserId(userId) > 0;
    }
}

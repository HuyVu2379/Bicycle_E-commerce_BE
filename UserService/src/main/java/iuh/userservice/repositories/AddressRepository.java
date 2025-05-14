package iuh.userservice.repositories;

import iuh.userservice.entities.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, String> {
    Optional<Address> findAddressByUserId(String userId);
    int deleteAddressByUserId(String userId);
    default Optional<Address> updateAddress(Address address) {
        return findById(address.getAddressId()).map(existingAddress -> {
            existingAddress.setFullAddress(address.getFullAddress());
            existingAddress.setCity(address.getCity());
            existingAddress.setStreet(address.getStreet());
            existingAddress.setWard(address.getWard());
            existingAddress.setCountry(address.getCountry());
            save(existingAddress);
            return existingAddress;
        });
    }
}

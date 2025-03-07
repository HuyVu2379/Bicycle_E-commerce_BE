package iuh.userservice.repositories;

import iuh.userservice.entities.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, String> {
    public Optional<Address> findAddressByUserId(String userId);
    public int deleteAddressByUserId(String userId);
}

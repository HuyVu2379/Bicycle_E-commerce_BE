package iuh.userservice.mappers;

import iuh.userservice.dtos.responses.AddressResponse;
import iuh.userservice.entities.Address;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    public AddressResponse AddressToAddressResponse(Address address);
}
